package nz.ac.wgtn.nullannoinference.propagator;

import com.google.common.base.Preconditions;
import com.google.gson.reflect.TypeToken;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.ProjectType;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Main class, entry point for executable jar that is being built.
 * @author jens dietrich
 */
public class Main {

    public static final boolean PROPAGATE_NULLABILITY_FOR_ARGUMENTS = true;

    // TODO make this configurable -- only those issues will be configured
    public static final Predicate<Issue> ISSUE_FILTER = issue -> issue.getScope() == Issue.Scope.MAIN;
    public static final Logger LOGGER = LogSystem.getLogger("refiner");

    public static void main (String[] args) throws Exception {

        Options options = new Options();
        options.addRequiredOption("i","input",true,"a json file with null issues (required)");
        options.addRequiredOption("p","project",true,"the folder containing the project to be analysed, the project must have been built (required)");
        options.addRequiredOption("o","output",true,"the json file where both input and inferred issues will be stored (required)");
        options.addRequiredOption("x","packagePrefix",true,"the prefix of packages for which the hierarchy will be analysed, such as \"org.apache.commons\" (required)");
        options.addOption("t","projecttype",true,"the project type, default is mvn (Maven), can be set to any of " + ProjectType.getValidProjectTypes());
        options.addOption("a","propagate4args",false,"whether to propagate nullability for arguments to subtypes (optional, default is " + PROPAGATE_NULLABILITY_FOR_ARGUMENTS + ")");

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

        File input= new File(cmd.getOptionValue("input"));
        Preconditions.checkArgument(input.exists(),"input file does not exist: " + input.getAbsolutePath());
        LOGGER.info("reading issues in: " + input.getAbsolutePath());

        File output= new File(cmd.getOptionValue("output"));
        LOGGER.info("existing and inferred issues will be saved in: " + output.getAbsolutePath());

        String packagePrefix = null;
        packagePrefix = cmd.getOptionValue("packagePrefix");
        LOGGER.info("prefix for classes / packages for which the hierarchy will be analysed: " + packagePrefix);


        boolean propagate4args = cmd.hasOption("propagate4args");


        LOGGER.info("Inferring additional nullability annotations for sub and super types");
        LOGGER.info("\tpropagate nullability to return types of overridden methods in supertypes: true");
        LOGGER.info("\tpropagate nullability to arguments type of overriding methods in subtypes: " + propagate4args);
        InferAdditionalIssues.run(projectType,input,projectFolder, output,packagePrefix, propagate4args,ISSUE_FILTER);

    }
}
