package nz.ac.wgtn.nullannoinference.annotator;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;

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
    public static final String ARG_ISSUES = "issues";
    public static final NullableAnnotationProvider DEFAULT_ANNOTATION_PROVIDER = new JSR305NullableAnnotationProvider();

    public static final Logger LOGGER = LogSystem.getLogger("annnotator");

    private static List<AnnotationListener> listeners = List.of(new LoggingAnnotationListener());

    public static void main(String[] args) throws ParseException, IOException {
        Options options = new Options();
        options.addRequiredOption("p",ARG_INPUT, true, "the input mvn project folder");
        options.addRequiredOption("o",ARG_OUTPUT, true, "the output mvn project folder (files will be override / emptied)");
        options.addOption("a",ARG_NULLABLE_ANNOTATION_PROVIDER,true,"the name of an annotation provider (pluggable, default is JSR305)");
        options.addRequiredOption("i",ARG_ISSUES,true,"a folder containing nullable issues (collected and refined)");

        CommandLineParser parser = new DefaultParser() {
            @Override
            protected void checkRequiredOptions() throws MissingOptionException {
                try {
                    super.checkRequiredOptions();
                }
                catch (MissingOptionException x) {
                    LOGGER.error("arguments missing",x);
                    // print help instructions
                    String header = "Adds null annotations to the classes of a Maven project\n\n";
                    String footer = "\nPlease report issues at https://github.com/jensdietrich/null-annotation-inference/issues";
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.printHelp(MvnProjectAnnotator.class.getName(), header, options, footer, true);
                    System.exit(1);
                }
            }
        };
        CommandLine cmd = parser.parse(options, args);

        File inputProjectFolder = new File(cmd.getOptionValue(ARG_INPUT));
        Preconditions.checkArgument(inputProjectFolder.exists());
        Preconditions.checkArgument(inputProjectFolder.isDirectory());
        Preconditions.checkArgument(new File(inputProjectFolder,"pom.xml").exists());

        File outputProjectFolder = new File(cmd.getOptionValue(ARG_OUTPUT));

        File issueFolder = new File(cmd.getOptionValue(ARG_ISSUES));
        Preconditions.checkArgument(issueFolder.exists());
        Preconditions.checkArgument(issueFolder.isDirectory());
        Preconditions.checkArgument(issueFolder.listFiles(f -> f.getName().endsWith(".json")).length>0);

        if (outputProjectFolder.exists()) {
            FileUtils.deleteDirectory(outputProjectFolder);
        }
        outputProjectFolder.mkdirs();

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
        Collection<File> issueFiles = FileUtils.listFiles(issueFolder,new String[]{"json"},true);
        LOGGER.info("Using nullable specs from the following files:");
        for (File spec:issueFiles) {
            LOGGER.info("\t"+spec.getAbsolutePath());
        }

        Preconditions.checkArgument(issueFiles.size()>0,"no matching null spec definition files found");
        issueFiles.stream().forEach(f -> Preconditions.checkArgument(f.exists()));

        Set<Issue> issues = new HashSet<>();
        issueFiles.stream().forEach(f -> {
            try {
                Issue[] array = new Gson().fromJson(new FileReader(f), Issue[].class);
                for (Issue spec:array) {
                    issues.add(spec);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });

        LOGGER.info(issues.size() + " nullable specs found");

        listeners.forEach(l -> l.beforeAnnotationTransformation(inputProjectFolder,outputProjectFolder));
        Files.walkFileTree(inputProjectFolder.toPath(), new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                String n = dir.getName(dir.getNameCount()-1).toString();
                boolean skip = dir.toFile().isHidden() || n.equals("target") || n.equals("pom-instrumented.xml") || n.equals("infer-out") ;
                return skip ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path rel = inputProjectFolder.toPath().relativize(file);
                File copy = outputProjectFolder.toPath().resolve(rel).toFile();
                LOGGER.info("Copying file to " + copy.getAbsolutePath());

                String n = file.getName(file.getNameCount()-1).toString();
                boolean skip = n.equals("pom-instrumented.xml") || n.startsWith("null-issues-observed");
                if (skip) return FileVisitResult.CONTINUE;

                // TODO do this only if necessary (i.e. if a class was actually transformed)
                boolean transformed = false;
                if (file.endsWith("pom.xml")) {
                    try {
                        transformed = pomDependencyInjector.addDependency(file.toFile(), copy);
                        if (transformed) {
                            LOGGER.info("pom.xml has been transformed");
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
                        annotationsAdded = annotationsAdded + classAnnotator.annotateMethod(file.toFile(),copy,issues);
                        transformed = annotationsAdded>0;
                    }
                    catch (AmbiguousAnonymousInnerClassResolutionException | JavaParserFailedException x) {
                        listeners.forEach(l -> l.annotationFailed(file.toFile(),x.getMessage()));
                    }

                    if (transformed) {
                        int annotationsAdded2 = annotationsAdded; // for use in lambda
                        LOGGER.info("" + file.toString() + " has been transformed, " + annotationsAdded2 + " annotations have been added");
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
        listeners.forEach(l -> l.afterAnnotationTransformation(inputProjectFolder,outputProjectFolder));

    }

    private static Predicate<File> SPEC_FILE_FILTER = f -> f.exists() && !f.isDirectory() && f.getName().endsWith(".json");

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
