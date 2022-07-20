package nz.ac.wgtn.nullannoinference.scoper;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.ProjectType;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.*;
import java.io.*;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * Add the scope attribute to issues by investigating the project structure.
 * Main class, entry point for executable jar that is being built.
 * @author jens dietrich
 */
public class Scoper {

    public static final Logger LOGGER = LogSystem.getLogger("main");

    // for summary
    public enum KEYS  {
        MAIN_CLASSES, MAIN_TOPLEVEL_CLASSES, MAIN_METHODS_ALL, MAIN_METHODS_NULLABLE, MAIN_RETURNS, MAIN_ARGUMENTS, MAIN_FIELDS_NULLABLE, MAIN_FIELDS_ALL, TEST_CLASSES, TEST_TOPLEVEL_CLASSES, TEST_METHODS_ALL, TEST_METHODS_NULLABLE, TEST_RETURNS, TEST_ARGUMENTS, TEST_FIELDS_NULLABLE, TEST_FIELDS_ALL
    }

    public static void main (String[] args) throws Exception {

        Options options = new Options();
        options.addRequiredOption("i","input",true,"a json file containing null issues (required)");
        options.addRequiredOption("o","output",true,"a json file where the issues with the scope attribute set will be saved (required)");
        options.addRequiredOption("p","project",true,"the folder containing the Maven project (i.e. containing pom.xml) to be analysed, the project must have been built with \"mvn test\" (required)");
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
                String header = "Adds scope to the nullability issues collected by the agent\n\n";
                String footer = "\nPlease report issues at https://github.com/jensdietrich/null-annotation-inference/issues";
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(Scoper.class.getName(), header, options, footer, true);
                System.exit(1);
            }
            }
        };
        CommandLine cmd = parser.parse(options, args);

        ProjectType project = ProjectType.getProject(cmd.getOptionValue("projecttype"));
        LOGGER.info("using project type: " + project.getType());

        // input validation
        File projectFolder = new File(cmd.getOptionValue("project"));
        Preconditions.checkArgument(projectFolder.exists(),"project folder does not exist: " + projectFolder.getAbsolutePath());
        Preconditions.checkArgument(projectFolder.isDirectory(),"project folder is not a folder: " + projectFolder.getAbsolutePath());
        project.checkProjectRootFolder(projectFolder);
        LOGGER.info("analysing project: " + projectFolder.getAbsolutePath());

        File issueFile = new File(cmd.getOptionValue("input"));
        Preconditions.checkArgument(issueFile.exists(),"input folder does not exist: " + issueFile.getAbsolutePath());
        LOGGER.info("processing null issues in: " + issueFile.getAbsolutePath());

        File outputFile = new File(cmd.getOptionValue("output"));

        Set<String> mainClassNames = new HashSet<>();
        Set<String> testClassNames = new HashSet<>();

        Collection<File> classFiles = project.getCompiledMainClasses(projectFolder);
        Preconditions.checkState(!classFiles.isEmpty(),"no main compiled classes found, check whether project has been built");
        for (File classFile:classFiles) {
            collectClassNames(classFile, mainClassNames);
        }
        classFiles = project.getCompiledTestClasses(projectFolder);
        Preconditions.checkState(!classFiles.isEmpty(),"no test compiled classes found, check whether project has been built");
        for (File classFile:classFiles) {
            collectClassNames(classFile, testClassNames);
        }

        // add scope to issues
        List<Issue> issues = null;
        List<Issue> modifiedIssues = new ArrayList<>();
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<Issue>>() {}.getType();
        try (Reader in = new FileReader(issueFile)) {
            issues = gson.fromJson(in, listType);
            for (Issue issue : issues) {
                String clazz = issue.getClassName();
                if (mainClassNames.contains(clazz)) {
                    issue.setScope(Issue.Scope.MAIN);
                    modifiedIssues.add(issue);
                } else if (testClassNames.contains(clazz)) {
                    issue.setScope(Issue.Scope.TEST);
                    modifiedIssues.add(issue);
                } else {
                    LOGGER.debug("Unclassifiable class found (neither main nor test -- might be from different project or synthetic): " + clazz + " -- will use " + Issue.Scope.OTHER);
                    issue.setScope(Issue.Scope.OTHER);
                    modifiedIssues.add(issue);
                }
            }
        }
        // write files back
        gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter out = new FileWriter(outputFile)) {
            gson.toJson(modifiedIssues, listType,out);
            LOGGER.info("Augmented issues with scope attribute written to " + outputFile.getAbsolutePath());
        }
    }


    static void collectClassNames(File classFile, Set<String> classes)  {
        try {
            ClassReader clReader = new ClassReader(new FileInputStream(classFile));
            clReader.accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                    super.visit(version, access, name, signature, superName, interfaces);
                    if ((access & Opcodes.ACC_SYNTHETIC) == 0) {
                        classes.add(name.replace('/', '.'));
                    }
                }
            },0);
        } catch (IOException e) {
            LOGGER.error("Cannot analyse class file: " + classFile.getAbsolutePath(),e);
        }
    }

}
