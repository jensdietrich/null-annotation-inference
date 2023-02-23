package nz.ac.wgtn.nullinference.extractor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.ProjectType;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Main class, entry point for executable jar that is being built.
 * @author jens dietrich
 */
public class Main {

    public static final String ARG_INPUT = "project";
    public static final String ARG_OUTPUT = "issues";
    public static final String ARG_PROJECT_TYPE = "projecttype";
    public static final String ARG_VOID_IS_NULLABLE = "voidisnullable";

    // TODO make this configurable -- only those issues will be configured
    public static final Predicate<Issue> ISSUE_FILTER = issue -> issue.getScope()== Issue.Scope.MAIN;

    public static final Logger LOGGER = LogSystem.getLogger("annotation-extractor");

    public static void main (String[] args) throws Exception {

        Options options = new Options();
        options.addRequiredOption("p",ARG_INPUT, true, "the input mvn project folder");
        options.addRequiredOption("o",ARG_OUTPUT, true, "the file name containing the issues collected (.json)");
        options.addRequiredOption("t", ARG_PROJECT_TYPE, true, "the project type (see nz.ac.wgtn.nullannoinference.commons.Project for valid types: mvn , gradle , ..)");
        options.addOption("v", ARG_VOID_IS_NULLABLE, false, "if set, java.lang.Void will be treated as implicitly annotated as nullable");

        CommandLineParser parser = new DefaultParser() {
            @Override
            protected void checkRequiredOptions() throws MissingOptionException {
            try {
                super.checkRequiredOptions();
            }
            catch (MissingOptionException x) {
                LOGGER.error("arguments missing",x);
                // print help instructions
                String header = "Collects existing annotations from a project\n\n";
                String footer = "\nPlease report issues at https://github.com/jensdietrich/null-annotation-inference/issues";
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(Main.class.getName(), header, options, footer, true);
                System.exit(1);
            }
            }
        };
        CommandLine cmd = parser.parse(options, args);

        // input validation
        File projectFolder = new File(cmd.getOptionValue(ARG_INPUT));
        Preconditions.checkArgument(projectFolder.exists(),"project folder does not exist: " + projectFolder.getAbsolutePath());
        Preconditions.checkArgument(projectFolder.isDirectory(),"project folder is not a folder: " + projectFolder.getAbsolutePath());

        String projectType = cmd.getOptionValue(ARG_PROJECT_TYPE);
        ProjectType project = ProjectType.getProject(projectType);
        project.checkProjectRootFolder(projectFolder);
        Preconditions.checkState(!project.getCompiledMainClasses(projectFolder).isEmpty(),"no compiled classes found in project, check whether project has been built: " + project.getCompiledMainClasses(projectFolder) );

        boolean voidIsNullable = cmd.hasOption(ARG_VOID_IS_NULLABLE);

        LOGGER.info("analysing project: " + projectFolder.getAbsolutePath());
        Set<Issue> issues = ExtractNullableAnnotations.findNullAnnotated(project,projectFolder);

        if (voidIsNullable) {
            LOGGER.info("looking for uses of java.lang.Void (tp be interpreted as nullable)");
            Set<Issue> issues2 = ExtractVoid.findNullAnnotated(project,projectFolder);
            issues = Sets.union(issues,issues2);
        }

        LOGGER.info("Existing issues found in analysed projects: " + issues.size());

        File issueFile = new File(cmd.getOptionValue(ARG_OUTPUT));
        Type gsonType = new TypeToken<Set<Issue>>() {}.getType();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter out = new FileWriter(issueFile)) {
            gson.toJson(issues,gsonType,out);
            LOGGER.info("Issues writing issues to file " + issueFile.getAbsolutePath());
        }
        catch (IOException x) {
            LOGGER.error("Error writing issues to file " + issueFile.getAbsolutePath(),x);
        }

    }
}
