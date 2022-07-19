package nz.ac.wgtn.nullannoinference.sanitizer;

import com.google.common.base.Preconditions;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.ProjectType;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Main class, entry point for executable jar that is being built.
 * @author jens dietrich
 */
public class Main {

    // TODO make this configurable -- only those issues will be configured
    public static final Predicate<Issue> ISSUE_FILTER = issue -> issue.getScope()== Issue.Scope.MAIN;
    public static final String NEGATIVE_TEST_SUMMARY_FILE_NAME = "negative-tests.csv";

    public static final Logger LOGGER = LogSystem.getLogger("refiner");

    public static void main (String[] args) throws Exception {

        Options options = new Options();
        options.addRequiredOption("i","input",true,"a json file with null issues (required)");
        options.addRequiredOption("p","project",true,"the folder containing the project to be analysed, the project must have been built (required)");
        options.addOption("n","negativetests",true,"the csv file where information about negative tests detected will be save in CSV format (optional, default is " + NEGATIVE_TEST_SUMMARY_FILE_NAME + ")");
        options.addRequiredOption("s","sanitisedissues",true,"a file where the sanitized issues will be saved (required)");
        options.addOption("t","projecttype",true,"the project type, default is mvn (Maven), can be set to any of " + ProjectType.getValidProjectTypes());

        CommandLineParser parser = new DefaultParser() {
            @Override
            protected void checkRequiredOptions() throws MissingOptionException {
            try {
                super.checkRequiredOptions();
            }
            catch (MissingOptionException x) {
                LOGGER.error("arguments missing",x);
                // print help instructions
                String header = "Refines the nullability issues collected by the agent\n\n";
                String footer = "\nPlease report issues at https://github.com/jensdietrich/null-annotation-inference/issues";
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(Main.class.getName(), header, options, footer, true);
                System.exit(1);
            }
            }
        };
        CommandLine cmd = parser.parse(options, args);

        // input validation
        File projectFolder = new File(cmd.getOptionValue("project"));
        Preconditions.checkArgument(projectFolder.exists(),"project folder does not exist: " + projectFolder.getAbsolutePath());
        Preconditions.checkArgument(projectFolder.isDirectory(),"project folder is not a folder: " + projectFolder.getAbsolutePath());
        LOGGER.info("analysing project: " + projectFolder.getAbsolutePath());

        ProjectType projectType = ProjectType.getProject(cmd.getOptionValue("projecttype"));
        LOGGER.info("using project type: " + projectType.getType());
        projectType.checkProjectRootFolder(projectFolder);

        File inputFile = new File(cmd.getOptionValue("input"));
        Preconditions.checkArgument(inputFile.exists(),"input file does not exist: " + inputFile.getAbsolutePath());
        LOGGER.info("using null issues in: " + inputFile.getAbsolutePath());

        String negTestFileName = null;
        if (cmd.hasOption("negativetests")) {
            negTestFileName = cmd.getOptionValue("negativetests");
        }
        else {
            negTestFileName = NEGATIVE_TEST_SUMMARY_FILE_NAME;
        }
        File negTestFile = new File(negTestFileName);

        File sanitisedIssues = null;
        if (cmd.hasOption("sanitisedissues")) {
            sanitisedIssues = new File(cmd.getOptionValue("sanitisedissues"));
        }
        LOGGER.info("sanitised issues will be saved in: " + sanitisedIssues.getAbsolutePath());


        Map<String,Integer> counts = new LinkedHashMap<>();

        LOGGER.info("Analysing negative tests");
        IdentifyNegativeTests.run(projectType,projectFolder,negTestFile);

        LOGGER.info("Removing issues caused by negative tests");
        SantitiseObservedIssues.run(inputFile,sanitisedIssues,negTestFile,ISSUE_FILTER);

        LOGGER.info("Summary of actions performed:");
        for (String key:counts.keySet()) {
            LOGGER.info(key + " : " + counts.get(key));
        }

    }
}
