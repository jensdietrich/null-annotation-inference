package nz.ac.wgtn.nullannoinference.sanitizer;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.ProjectType;
import nz.ac.wgtn.nullannoinference.sanitizer.deprecation.DeprecatedElementsSanitizer;
import nz.ac.wgtn.nullannoinference.sanitizer.mainscope.MainScopeSanitizer;
import nz.ac.wgtn.nullannoinference.sanitizer.negtests.NegativeTestSanitizer;
import nz.ac.wgtn.nullannoinference.sanitizer.shaded.ShadingSanitizer;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Main class, entry point for executable jar that is being built.
 * @author jens dietrich
 */
public class Main {

    public static final Type ISSUE_SET_TYPE = new TypeToken<Set<Issue>>() {}.getType();
    public static final Logger LOGGER = LogSystem.getLogger("refiner");

    public static void main (String[] args) throws Exception {
        Options options = new Options();
        options.addRequiredOption("i","input",true,"a json file with null issues (required)");
        options.addRequiredOption("p","project",true,"the folder containing the project to be analysed, the project must have been built (required)");
        options.addRequiredOption("o","sanitisedissues",true,"a file where the sanitized issues will be saved (required)");
        options.addOption("t","projecttype",true,"the project type, default is mvn (Maven), can be set to any of " + ProjectType.getValidProjectTypes());
        options.addOption("n","removeissuesfromnegativetests",false,"if set, perform an analysis to remove issues observed while executing negative tests");
        options.addOption("s","removeissuesinshadedclasses",false,"if set, perform an analysis to remove issues in shaded classes");
        options.addOption("d","removeissuesindeprecatedelements",false,"if set, perform an analysis to remove issues in deprecated elements");
        options.addOption("m","removeissuesnotinmain",false,"if set, issues in classes not in main scope are removed");
        options.addOption("nt","negativetests",true,"the csv file where information about negative tests detected will be saved in CSV format (optional)");
        options.addOption("sh","shadingspecs",true,"the json file with definitions of shaded packages, required for shading analysis");
        options.addOption("de","deprecatedelements",true,"the text file where information about deprecated elements found will be written (optional)");

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

        boolean removeIssuesFromNegativeTests = cmd.hasOption("removeissuesfromnegativetests");
        boolean removeIssuesInShadedclasses = cmd.hasOption("removeissuesinshadedclasses");
        boolean removeIssuesIndeprecatedElements = cmd.hasOption("removeissuesindeprecatedelements");
        boolean removeIssuesNotInMain = cmd.hasOption("removeissuesnotinmain");
        Preconditions.checkArgument(removeIssuesFromNegativeTests
                || removeIssuesInShadedclasses
                || removeIssuesIndeprecatedElements
                || removeIssuesNotInMain
                ,"no sanitizer option (-remove*) set");

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

        File shadingSpecs = null;
        if (cmd.hasOption("shadingspecs")) {
            shadingSpecs = new File(cmd.getOptionValue("shadingspecs"));
            LOGGER.info("using shading spec: " + shadingSpecs.getAbsolutePath());
        }
        Preconditions.checkArgument (!removeIssuesInShadedclasses || (shadingSpecs!=null && shadingSpecs.exists()),"shading spec is required to remove issues in shaded classes");

        String negTestFileName = null;
        if (cmd.hasOption("negativetests")) {
            negTestFileName = cmd.getOptionValue("negativetests");
        }
        File negTestFile = negTestFileName==null?null:new File(negTestFileName);


        String deprecatedElementsFileName = null;
        if (cmd.hasOption("deprecatedelements")) {
            deprecatedElementsFileName = cmd.getOptionValue("deprecatedelements");
        }
        File deprecatedElementsFile = deprecatedElementsFileName==null?null:new File(deprecatedElementsFileName);

        File sanitisedIssuesFile = null;
        if (cmd.hasOption("sanitisedissues")) {
            sanitisedIssuesFile = new File(cmd.getOptionValue("sanitisedissues"));
        }
        LOGGER.info("sanitised issues will be saved in: " + sanitisedIssuesFile.getAbsolutePath());

        // load unsanitized issues


        Gson gson = new Gson();
        Set<Issue> issues = null;
        try (FileReader in = new FileReader(inputFile)) {
            issues = gson.fromJson(in,ISSUE_SET_TYPE);
            LOGGER.info(issues.size() + " issues read from " + inputFile.getAbsolutePath());
        }
        catch (Exception x) {
            LOGGER.error("Error reading issues from file " + inputFile.getAbsolutePath(),x);
        }


        Sanitizer<Issue> sanitizer = Sanitizer.ALL;
        if (removeIssuesNotInMain) {
            MainScopeSanitizer mainScopeSanitizer = new MainScopeSanitizer(projectType, projectFolder);
            LOGGER.info("adding sanitizer: " + mainScopeSanitizer.getClass().getName());
            sanitizer = sanitizer.and(mainScopeSanitizer);
            LOGGER.info("setting main scope analyser to remove issues in classes not in main scope");
        }
        if (removeIssuesFromNegativeTests) {;
            NegativeTestSanitizer negativeTestSanitizer = new NegativeTestSanitizer(projectType, projectFolder, negTestFile);
            LOGGER.info("adding sanitizer: " + negativeTestSanitizer.getClass().getName());
            sanitizer = sanitizer.and(negativeTestSanitizer);
            LOGGER.info("setting remove issues from negative test analyser to remove issues observed in the execution of negative tests");
        }
        if (removeIssuesInShadedclasses) {
            ShadingSanitizer shadedSanitizer = new ShadingSanitizer(shadingSpecs);
            LOGGER.info("adding sanitizer: " + shadedSanitizer.getClass().getName());
            sanitizer = sanitizer.and(shadedSanitizer);
            LOGGER.info("setting shaded analyser to remove issues in classes in shaded packages");
        }
        if (removeIssuesIndeprecatedElements) {
            DeprecatedElementsSanitizer deprecatedSanitizer = new DeprecatedElementsSanitizer(projectType,projectFolder,deprecatedElementsFile);
            LOGGER.info("adding sanitizer: " + deprecatedSanitizer.getClass().getName());
            sanitizer = sanitizer.and(deprecatedSanitizer);
            LOGGER.info("setting deprecated analyser to remove issues in deprecated elements");
        }

        Sanitizer<Issue> sanitizer2 = sanitizer;

        LOGGER.info("using combined sanitizer: " + sanitizer2.name());

        Set<Issue> sanitisedIssues = issues.parallelStream()
            .filter(issue -> Sanitizer.sanitize(issue,sanitizer2))
            .collect(Collectors.toSet());

        LOGGER.info("sanitizer applied: " + sanitizer2.name());
        LOGGER.info("issues sanitized: " + sanitisedIssues.size() + " / " + issues.size());

        gson = new GsonBuilder().setPrettyPrinting().create();
        // even if empty, write file
        LOGGER.info("\twriting sanitised set to " + sanitisedIssuesFile.getAbsolutePath());
        if (!sanitisedIssuesFile.getParentFile().exists()) {
            sanitisedIssuesFile.getParentFile().mkdirs();
        }

        try (Writer out = new FileWriter(sanitisedIssuesFile)) {
            gson.toJson(sanitisedIssues, out);
        }
        catch (Exception x) {
            LOGGER.error("Error writing issues to " + sanitisedIssuesFile.getAbsolutePath());
        }

    }

}
