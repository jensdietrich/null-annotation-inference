package nz.ac.wgtn.nullannoinference.deprecationanalyser;

import com.google.common.base.Preconditions;
import nz.ac.wgtn.nullannoinference.commons.ProjectType;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.Logger;
import java.io.File;

/**
 * Main class, entry point for executable jar that is being built.
 * @author jens dietrich
 */
public class Main {


    public static final String DEPRECATED_ELEMENTS_FILE_NAME = "deprecated-elements.txt";

    public static final Logger LOGGER = LogSystem.getLogger("deprecation-analyses");

    public static void main (String[] args) throws Exception {

        Options options = new Options();
        options.addRequiredOption("p","project",true,"the folder containing the project to be analysed, the project must have been built (required)");
        options.addOption("o","output",true,"the text file where information about deprecated files will be saved (optional, default is " + DEPRECATED_ELEMENTS_FILE_NAME + ")");
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


        String deprecatedElementsFileName = null;
        if (cmd.hasOption("output")) {
            deprecatedElementsFileName = cmd.getOptionValue("output");
        }
        else {
            deprecatedElementsFileName = DEPRECATED_ELEMENTS_FILE_NAME;
        }

        LOGGER.info("Analysing project for deprecated elements");
        ExtractDeprecatedElements.run(projectType,projectFolder,new File(deprecatedElementsFileName));


    }
}
