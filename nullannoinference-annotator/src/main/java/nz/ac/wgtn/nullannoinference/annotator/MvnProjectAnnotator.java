package nz.ac.wgtn.nullannoinference.annotator;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Predicate;

/**
 * Annotator for entire maven projects.
 * @author jens dietrich
 */
public class MvnProjectAnnotator {

    // arg defs
    public static final String ARG_INPUT = "input";
    public static final String ARG_OUTPUT = "output";
    public static final String ARG_NULLABLE_ANNOTATION_PROVIDER = "annotationprovider";
    public static final String ARG_NULLABLE_SPECS = "nullablespecs";
    public static final NullableAnnotationProvider DEFAULT_ANNOTATION_PROVIDER = new JSR305NullableAnnotationProvider();

    private static List<AnnotationListener> listeners = List.of(new LoggingAnnotationListener());

    public static void main(String[] args) throws ParseException, IOException {
        Options options = new Options();
        options.addOption(ARG_INPUT, true, "the input mvn project folder");
        options.addOption(ARG_OUTPUT, true, "the output mvn project folder (files will be override / emptied)");
        options.addOption(ARG_NULLABLE_ANNOTATION_PROVIDER,true,"the name of an annotation provider (pluggable, default is JSR305)");
        options.addOption(ARG_NULLABLE_SPECS,true,"name of file(s) with json-encoded nullable specs, can use wildcards (*,?) in name");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        String inputOption = cmd.getOptionValue(ARG_INPUT);
        String outputOption = cmd.getOptionValue(ARG_OUTPUT);
        String specOption = cmd.getOptionValue(ARG_NULLABLE_SPECS);

        if (inputOption==null || outputOption==null || specOption==null) {
            if (inputOption==null) System.out.println("missing value for argument " + ARG_INPUT);
            if (outputOption==null) System.out.println("missing value for argument " + ARG_OUTPUT);
            if (specOption==null) System.out.println("missing value for argument " + ARG_NULLABLE_SPECS);
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -cp <cp> " + MvnProjectAnnotator.class.getName(), options);
            System.exit(1);
        }

        File input = new File(inputOption);
        File output = new File(outputOption);
        Preconditions.checkArgument(input.exists());
        Preconditions.checkArgument(input.isDirectory());
        Preconditions.checkArgument(new File(input,"pom.xml").exists());

        if (output.exists()) {
            FileUtils.deleteDirectory(output);
        }
        output.mkdirs();

        // locate nullannotationprovider
        NullableAnnotationProvider annotationProvider = null;
        String annotationProviderName = cmd.getOptionValue(ARG_NULLABLE_ANNOTATION_PROVIDER);
        if (annotationProviderName==null) {
            annotationProvider = DEFAULT_ANNOTATION_PROVIDER;
        }
        else {
            ServiceLoader.Provider<NullableAnnotationProvider> pr = ServiceLoader.load(NullableAnnotationProvider.class)
                .stream().filter(s -> s.get().getName().equals(annotationProviderName))
                .findFirst().get();
            Preconditions.checkArgument(pr!=null,"no NullableAnnotationProvider implementation found for name " + annotationProviderName);
            annotationProvider = pr.get();
        }
        ClassAnnotator classAnnotator = new ClassAnnotator(annotationProvider);
        POMDependencyInjector pomDependencyInjector = new POMDependencyInjector(annotationProvider);

        // check nullable options
        List<File> nullableSpecsFiles = Utils.getFiles(specOption);
        nullableSpecsFiles = collectSpecsRecursively(nullableSpecsFiles);
        System.out.println("Using nullable specs from the following files:");
        for (File spec:nullableSpecsFiles) {
            System.out.println("\t"+spec.getAbsolutePath());
        }

        Preconditions.checkArgument(nullableSpecsFiles.size()>0,"no matching null spec definition files found");
        nullableSpecsFiles.stream().forEach(f -> Preconditions.checkArgument(f.exists()));

        Set<NullableSpec> nullableSpecs = new HashSet<>();
        nullableSpecsFiles.stream().forEach(f -> {
            try {
                NullableSpec[] array = new Gson().fromJson(new FileReader(f), NullableSpec[].class);
                for (NullableSpec spec:array) {
                    nullableSpecs.add(spec);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });

        System.out.println(nullableSpecs);

        listeners.forEach(l -> l.beforeAnnotationTransformation(input,output));
        Files.walkFileTree(input.toPath(), new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                String n = dir.getName(dir.getNameCount()-1).toString();
                boolean skip = dir.toFile().isHidden() || n.equals("target") || n.equals("pom-instrumented.xml") || n.equals("infer-out") ;
                return skip ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path rel = input.toPath().relativize(file);
                File copy = output.toPath().resolve(rel).toFile();
                System.out.println("Copying file to " + copy.getAbsolutePath());

                String n = file.getName(file.getNameCount()-1).toString();
                boolean skip = n.equals("pom-instrumented.xml") || n.startsWith("null-issues-observed");
                if (skip) return FileVisitResult.CONTINUE;

                // TODO do this only if necessary (i.e. if a class was actually transformed)
                boolean transformed = false;
                if (file.endsWith("pom.xml")) {
                    try {
                        transformed = pomDependencyInjector.addDependency(file.toFile(), copy);
                        if (transformed) {
                            System.out.println("pom.xml has been transformed");
                            listeners.forEach(l -> l.configFileTransformed(file.toFile(),copy));
                        }
                    }
                    catch (Exception x) {
                        // TODO proper logging
                        x.printStackTrace();
                    }
                }
                if (file.toString().endsWith(".java")) {
                    int annotationsAdded = 0;
                    try {
                        annotationsAdded = annotationsAdded + classAnnotator.annotateMethod(file.toFile(),copy,nullableSpecs);
                        transformed = annotationsAdded>0;
                    }
                    catch (AmbiguousAnonymousInnerClassResolutionException | JavaParserFailedException x) {
                        listeners.forEach(l -> l.annotationFailed(file.toFile(),x.getMessage()));
                    }

                    if (transformed) {
                        int annotationsAdded2 = annotationsAdded; // for use in lambda
                        System.out.println("" + file.toString() + " has been transformed, " + annotationsAdded2 + " annotations have been added");
                        listeners.forEach(l -> l.annotationsAdded(file.toFile(),copy,annotationsAdded2));
                    }
                }

                if (!transformed) {
                    // copy
                    FileUtils.copyFile(file.toFile(), copy);
                    listeners.forEach(l -> l.fileCopied(file.toFile(),copy));
                }
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        });
        listeners.forEach(l -> l.afterAnnotationTransformation(input,output));

    }

    private static Predicate<File> SPEC_FILE_FILTER = f -> f.exists() && !f.isDirectory() && f.getName().endsWith(".json");
    private static List<File> collectSpecsRecursively(List<File> nullableSpecsFiles) {
        List<File> files = new ArrayList<>();
        for (File file:nullableSpecsFiles) {
            addSpec(files,file);
        }
        return files;
    }
    private static void addSpec(List<File> files,File specOfFolder) {
        if (SPEC_FILE_FILTER.test(specOfFolder)) {
            files.add(specOfFolder);
        }
        else if (specOfFolder.isDirectory()) {
            for (File f:specOfFolder.listFiles()) {
                addSpec(files,f);
            }
        }
    }
}
