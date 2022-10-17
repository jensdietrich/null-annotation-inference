package nz.ac.wgtn.nullannoinference.annotator;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import nz.ac.wgtn.nullannoinference.commons.*;
import nz.ac.wgtn.nullannoinference.commonsio.IssueIO;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Annotator for entire projects.
 * @author jens dietrich
 */

public class Main {

    // arg defs
    public static final String ARG_INPUT = "input";
    public static final String ARG_OUTPUT = "output";
    public static final String ARG_NULLABLE_ANNOTATION_PROVIDER = "annotationprovider";
    public static final String ARG_ISSUES = "issues";
    public static final String ARG_PROJECT_NAME = "name";
    public static final String ARG_PROJECTTYPE = "projecttype";
    public static final NullableAnnotationProvider DEFAULT_ANNOTATION_PROVIDER = new JSR305NullableAnnotationProvider();

    public static final Logger LOGGER = LogSystem.getLogger("annnotator");

    private static LoggingAnnotationListener LOGGING_LISTENER = new LoggingAnnotationListener();
    private static List<AnnotationListener> listeners = List.of(LOGGING_LISTENER);

    private static IssueInstanceSelector issueSelector = new DefaultIssueInstanceSelector();

    public static void main(String[] args) throws ParseException, IOException {
        Options options = new Options();
        options.addRequiredOption("p",ARG_INPUT, true, "the input project folder");
        options.addRequiredOption("o",ARG_OUTPUT, true, "the output project folder (files will be overridden)");
        options.addOption("a",ARG_NULLABLE_ANNOTATION_PROVIDER,true,"the name of an annotation provider (optional, default is JSR305)");
        options.addRequiredOption("i",ARG_ISSUES,true,"a file containing nullable issues (json format)");
        options.addOption("n",ARG_PROJECT_NAME,true,"the name of the project (to be used to filter out issues in upstream libraries) (optional)");
        options.addRequiredOption("t",ARG_PROJECTTYPE,true,"the project type, can be set to any of " + ProjectType.getValidProjectTypesAsString());

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
                    formatter.printHelp(Main.class.getName(), header, options, footer, true);
                    System.exit(1);
                }
            }
        };
        CommandLine cmd = parser.parse(options, args);

        String projectName = cmd.getOptionValue(ARG_PROJECT_NAME);

        File inputProjectFolder = new File(cmd.getOptionValue(ARG_INPUT));
        Preconditions.checkArgument(inputProjectFolder.exists());
        Preconditions.checkArgument(inputProjectFolder.isDirectory());

        File outputProjectFolder = new File(cmd.getOptionValue(ARG_OUTPUT));

        ProjectType projectType = ProjectType.getProject(cmd.getOptionValue(ARG_PROJECTTYPE));
        LOGGER.info("using project type: " + projectType.getType());
        projectType.checkProjectRootFolder(inputProjectFolder);

        File issueFile = new File(cmd.getOptionValue(ARG_ISSUES));
        Preconditions.checkArgument(issueFile.exists());
        Preconditions.checkArgument(!issueFile.isDirectory());

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
            NullableAnnotationProvider[] annotationProviders = {new JSR305NullableAnnotationProvider(),new SpringFrameworkNullableAnnotationProvider()};
            for (NullableAnnotationProvider provider:annotationProviders) {
                if (provider.getName().equals(annotationProviderName)) {
                    annotationProvider = provider;
                }
            }
            if (annotationProvider==null) {
                LOGGER.warn("No nullable annotation provider found for name " + annotationProviderName + " - using default " + DEFAULT_ANNOTATION_PROVIDER.getName());
                annotationProvider = DEFAULT_ANNOTATION_PROVIDER;
            }
        }
        final NullableAnnotationProvider annotationProvider2 = annotationProvider; // to make final for access from inner classes
        ClassAnnotator classAnnotator = new ClassAnnotator(annotationProvider);

        Predicate<Issue> filterByContext = null;
        if (projectName==null) {
            filterByContext = issue -> true;
        }
        else {
            filterByContext = issue -> issue.getContext()==null || Objects.equals(issue.getContext(),projectName);
        }

        Set<Issue> issues = IssueIO.readIssues(issueFile,filterByContext).stream().collect(Collectors.toSet());
        Preconditions.checkArgument(issues.size()>0,"no matching nullable issues found");

        LOGGER.info(issues.size() + " nullability issues to be processed imported");
        Set<Issue> aggregatedIssues = aggregateIssues(issues);
        LOGGER.info(aggregatedIssues.size() + " aggregated nullability issues to be processed");
        listeners.forEach(l -> l.issuesLoaded(aggregatedIssues));
        listeners.forEach(l -> l.beforeAnnotationTransformation(inputProjectFolder,outputProjectFolder));
        final AtomicBoolean buildScriptEdited = new AtomicBoolean(false); // object to make final
        Files.walkFileTree(inputProjectFolder.toPath(), new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                String n = dir.getName(dir.getNameCount()-1).toString();
                boolean skip = dir.toFile().isHidden() || n.equals("target") || n.equals("pom-instrumented.xml") || n.equals("infer-out")   ;
                return skip ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path rel = inputProjectFolder.toPath().relativize(file);
                File copy = outputProjectFolder.toPath().resolve(rel).toFile();
                LOGGER.info("Copying file to " + copy.getAbsolutePath());

                String n = file.getName(file.getNameCount()-1).toString();
                boolean skip = n.equals("pom-instrumented.xml") || n.startsWith("null-issues-observed"); // experiment specific -- TODO clean up
                if (skip) return FileVisitResult.CONTINUE;


                boolean transformed = false;

                // TODO use a more "pluggable design"
                if (file.endsWith("pom.xml") && projectType==ProjectType.MVN) {
                    try {
                        transformed = new POMDependencyInjector(annotationProvider2).addDependency(file.toFile(), copy);
                        if (transformed) {
                            LOGGER.info("pom.xml has been transformed");
                            listeners.forEach(l -> l.configFileTransformed(file.toFile(),copy));
                        }
                        buildScriptEdited.set(true);
                    }
                    catch (Exception x) {
                        LOGGER.error("exception modifying mvn build file " + file.toFile().getAbsolutePath(),x);
                    }
                }
                // skip tests !
                if (file.toString().endsWith(".java") && ! file.toString().contains("src/test/java")) {
                    int annotationsAdded = 0;
                    try {
                        annotationsAdded = annotationsAdded + classAnnotator.annotateMembers(file.toFile(),copy,aggregatedIssues,listeners);
                        transformed = annotationsAdded>0;
                    }
                    catch (JavaParserFailedException x) {
                        listeners.forEach(l -> l.annotationFailed(file.toFile(),x.getMessage()));
                    }

                    if (transformed) {
                        int annotationsAdded2 = annotationsAdded; // for use in lambda
                        LOGGER.info("" + file.toString() + " has been transformed, " + annotationsAdded2 + " annotations have been added");
                    }
                }

                if (!transformed) {

                    // filter out instrumentation-related files
                    String name = file.toFile().getName();
                    if (name.startsWith("nullannoinference-") || name.equals("pom-instrumented.xml")) {
                        LOGGER.warn("ignoring " + file.toFile().getAbsolutePath());
                    }

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

        if (!buildScriptEdited.get()) {
            LOGGER.warn("Build script has not been edited, it might be necessary to add dependency to library providing @Nullable manually");
            LOGGER.warn("\tmvn package id: " + annotationProvider2.getNullableAnnotationArtifactGroupId());
            LOGGER.warn("\tmvn artifact id: " + annotationProvider2.getNullableAnnotationArtifactId());
        }

    }

    /*
     * Select issues to process by picking one for each class of equivalent issues. This avoids redundancies.
     */
    private static Set<Issue> aggregateIssues(Set<Issue> issues) {
        // compute equivalence classes
        Multimap<IssueKernel,Issue> eqClasses = HashMultimap.create();
        for (Issue issue:issues) {
            eqClasses.put(issue.getKernel(),issue);
        }

        // pick representative
        HashSet<Issue> selectedIssues = new HashSet<>();
        for (IssueKernel kernel:eqClasses.keySet()) {
            Set<Issue> instances = (Set)eqClasses.get(kernel);
            selectedIssues.add(issueSelector.pick(kernel,instances));
        }
        return selectedIssues;
    }

    private static Predicate<File> SPEC_FILE_FILTER = f -> f.exists() && !f.isDirectory() && f.getName().endsWith(".json");

}
