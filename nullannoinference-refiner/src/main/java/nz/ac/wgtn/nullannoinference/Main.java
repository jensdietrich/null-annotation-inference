package nz.ac.wgtn.nullannoinference;

import com.google.common.base.Preconditions;
import nz.ac.wgtn.nullannoinference.negtests.IdentifyNegativeTests;
import nz.ac.wgtn.nullannoinference.negtests.SantitiseObservedIssues;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Main class, entry point for executable jar that is being built.
 * @author jens dietrich
 */
public class Main {

    // defaults
    public static final String NEGATIVE_TEST_SUMMARY_FILE_NAME = "negative-tests.csv";
    public static final String SANITISED_NULLABILITY_ISSUES = "sanitised_nullability_issues";
    public static final boolean PROPAGATE_NULLABILITY_FOR_ARGUMENTS = false;

    public static final Logger LOGGER = LogSystem.getLogger("main");

    public static void main (String[] args) throws Exception {



        Options options = new Options();

        options.addRequiredOption("i","input",true,"a folder containing json files with null issues reported by a test run instrumented with the nullannoinference agent, the folder will be checked recursively for files (required)");
        options.addRequiredOption("p","project",true,"the folder containing the Maven project (i.e. containing pom.xml) to be analysed, the project must have been built with \"mvn test\" (required)");
        options.addOption("n","negativetests",true,"a csv file where information about negative tests detected will be save in CSV format (optional, default is " + NEGATIVE_TEST_SUMMARY_FILE_NAME + ")");
        options.addOption("s","sanitisedissues",true,"a folder where the issues not removed by the negative test sanitisation will be saved (optional, default is " + SANITISED_NULLABILITY_ISSUES + ")");
        options.addOption("a","propagatenullabilityforarguments",true,"whether to propagate nullability for arguments to subtypes (optional, default is " + PROPAGATE_NULLABILITY_FOR_ARGUMENTS + ")");

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
                    String footer = "\nPlease report issues at http://example.com/issues";
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
        Preconditions.checkArgument(new File(projectFolder,"pom.xml").exists(),"project folder is not a maven project (no pom.xml found): " + projectFolder.getAbsolutePath());
        Preconditions.checkArgument(new File(projectFolder,"target/classes").exists(),"project has not been built (no target/classes found): " + projectFolder.getAbsolutePath());
        Preconditions.checkArgument(new File(projectFolder,"target/test-classes").exists(),"project has not been built (no target/test-classes found, must be built with \"mvn test\" or \"mvn test-compile\"): " + projectFolder.getAbsolutePath());
        LOGGER.info("analysing project: " + projectFolder.getAbsolutePath());

        File inputFolder = new File(cmd.getOptionValue("input"));
        Preconditions.checkArgument(inputFolder.exists(),"input folder does not exist: " + inputFolder.getAbsolutePath());
        Preconditions.checkArgument(inputFolder.isDirectory(),"input folder is not a folder: " + inputFolder.getAbsolutePath());
        Collection<File> issueFiles = FileUtils.listFiles(inputFolder,new String[]{"json"},true);
        // coarse and unsound check might be empty
        Preconditions.checkArgument(issueFiles.size()>0,"no files containing nullability issues found in folder " + inputFolder.getAbsolutePath());
        LOGGER.info("using null issues in: " + inputFolder.getAbsolutePath());

        File negativeTestSummaryFile = null;
        if (cmd.hasOption("negativetests")) {
            negativeTestSummaryFile = new File(cmd.getOptionValue("negativetests"));
        }
        else {
            negativeTestSummaryFile = new File(NEGATIVE_TEST_SUMMARY_FILE_NAME);
        }
        LOGGER.info("using this file to store negative tests: " + negativeTestSummaryFile.getAbsolutePath());

        File sanitisedIssuesFolder = null;
        if (cmd.hasOption("sanitisedissues")) {
            sanitisedIssuesFolder = new File(cmd.getOptionValue("sanitisedissues"));
        }
        else {
            sanitisedIssuesFolder = new File(SANITISED_NULLABILITY_ISSUES);
        }
        LOGGER.info("sanitised issues will be saved in: " + sanitisedIssuesFolder.getAbsolutePath());


        String projectName = projectFolder.getName();
        Map<String,Integer> counts = new LinkedHashMap<>();

        LOGGER.info("Analysing negative tests");
        IdentifyNegativeTests.run(projectFolder,negativeTestSummaryFile,counts);

        LOGGER.info("Removing issues caused by negative tests");
        SantitiseObservedIssues.run(inputFolder,sanitisedIssuesFolder,negativeTestSummaryFile,counts);

        LOGGER.info("Summary of actions performed");
        for (String key:counts.keySet()) {
            LOGGER.info(key + " : " + counts.get(key));
        }


    }
}
