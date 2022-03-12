package nz.ac.wgtn.nullannoinference.scoper;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import nz.ac.wgtn.nullannoinference.commons.GradleProject;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.MavenProject;
import nz.ac.wgtn.nullannoinference.commons.Project;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.*;
import java.io.*;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Add the scope attribute to issues by investigating the project structure.
 * Assumes that the project has the standard Maven layout.
 * Will also produce a useful summary of the number of reference returns, method parameters and fields in main and test classes.
 * Main class, entry point for executable jar that is being built.
 * @author jens dietrich
 */
public class Scoper {

    // defaults
    public static final String SUMMARY_FILE_NAME = "byte-code-summary.csv";

    public static final Logger LOGGER = LogSystem.getLogger("main");

    // for summary
    public enum KEYS  {
        MAIN_CLASSES, MAIN_TOPLEVEL_CLASSES, MAIN_METHODS_ALL, MAIN_METHODS_NULLABLE, MAIN_RETURNS, MAIN_ARGUMENTS, MAIN_FIELDS_NULLABLE, MAIN_FIELDS_ALL, TEST_CLASSES, TEST_TOPLEVEL_CLASSES, TEST_METHODS_ALL, TEST_METHODS_NULLABLE, TEST_RETURNS, TEST_ARGUMENTS, TEST_FIELDS_NULLABLE, TEST_FIELDS_ALL
    }

    public static void main (String[] args) throws Exception {

        Options options = new Options();
        options.addRequiredOption("i","input",true,"a folder containing json files with null issues reported by a test run instrumented with the nullannoinference agent, the folder will be checked recursively for files (required)");
        options.addRequiredOption("p","project",true,"the folder containing the Maven project (i.e. containing pom.xml) to be analysed, the project must have been built with \"mvn test\" (required)");
        options.addOption("s","summary",true,"a summary csv file with some stats about the project bytecode analysed (optional, default is \"" + SUMMARY_FILE_NAME + "\")");
        options.addOption("g","gradle",false,"if set, gradle instead of maven conventions are used to locate compiled classes");

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

        Project project = new MavenProject();
        if (cmd.hasOption("gradle")) {
            project = new GradleProject();
        }
        LOGGER.info("using project type: " + project.getType());

        // input validation
        File projectFolder = new File(cmd.getOptionValue("project"));
        Preconditions.checkArgument(projectFolder.exists(),"project folder does not exist: " + projectFolder.getAbsolutePath());
        Preconditions.checkArgument(projectFolder.isDirectory(),"project folder is not a folder: " + projectFolder.getAbsolutePath());
        project.checkProjectRootFolder(projectFolder);
        LOGGER.info("analysing project: " + projectFolder.getAbsolutePath());

        File inputFolder = new File(cmd.getOptionValue("input"));
        Preconditions.checkArgument(inputFolder.exists(),"input folder does not exist: " + inputFolder.getAbsolutePath());
        Preconditions.checkArgument(inputFolder.isDirectory(),"input folder is not a folder: " + inputFolder.getAbsolutePath());
        Collection<File> issueFiles = FileUtils.listFiles(inputFolder,new String[]{"json"},true);
        // coarse and unsound check might be empty
        Preconditions.checkArgument(issueFiles.size()>0,"no files containing nullability issues found in folder " + inputFolder.getAbsolutePath());
        LOGGER.info("using null issues in: " + inputFolder.getAbsolutePath());

        File summaryFile = null;
        if (cmd.hasOption("summary")) {
            summaryFile = new File(cmd.getOptionValue("summary"));
        }
        else {
            summaryFile = new File(inputFolder,SUMMARY_FILE_NAME);
        }
        // LOGGER.info("a summary of byte code features will be written to: " + summaryFile.getAbsolutePath());

        // initialise counter
        Map<KEYS,Integer> counters = new LinkedHashMap<>();
        for (KEYS k:KEYS.values()) {
            counters.put(k,0);
        }

        Set<String> mainClassNames = new HashSet<>();
        Set<String> testClassNames = new HashSet<>();

        File classLocation = project.getCompiledMainClassesFolder(projectFolder);
        Collection<File> classFiles = FileUtils.listFiles(classLocation,new String[]{"class"},true);
        for (File classFile:classFiles) {
            analyseBytecodeForFeatures(classFile, Issue.Scope.MAIN,counters,mainClassNames);
        }
        classLocation = project.getCompiledTestClassesFolder(projectFolder);
        classFiles = FileUtils.listFiles(classLocation,new String[]{"class"},true);
        for (File classFile:classFiles) {
            analyseBytecodeForFeatures(classFile, Issue.Scope.TEST,counters,testClassNames);
        }

        // add scope to issues
        for (File issueFile:issueFiles) {
            List<Issue> issues = null;
            List<Issue> modifiedIssues = new ArrayList<>();
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<Issue>>() {}.getType();
            try (Reader in = new FileReader(issueFile)) {
                issues = gson.fromJson(in, listType);
                for (Issue issue:issues) {
                    String clazz = issue.getClassName();
                    if (mainClassNames.contains(clazz)) {
                        issue.setScope(Issue.Scope.MAIN);
                        modifiedIssues.add(issue);
                    }
                    else if (testClassNames.contains(clazz)) {
                        issue.setScope(Issue.Scope.TEST);
                        modifiedIssues.add(issue);
                    }
                    else {
                        LOGGER.debug("Unclassifiable class found (neither main nor test -- might be from different project or synthetic): " + clazz + " -- will use " + Issue.Scope.OTHER);
                        issue.setScope(Issue.Scope.OTHER);
                        modifiedIssues.add(issue);
                    }
                }
            }

            // write files back
            gson = new GsonBuilder().setPrettyPrinting().create();
            try (FileWriter out = new FileWriter(issueFile)) {
                gson.toJson(modifiedIssues, listType,out);
                LOGGER.info("Augmented issues with scope attribute written to " + issueFile.getAbsolutePath());
            }
        }

        // write summary
        try (PrintWriter out = new PrintWriter(new FileWriter(summaryFile))) {
            String keys = counters.keySet().stream().map(k -> k.name()).collect(Collectors.joining("\t"));
            String values = counters.keySet().stream().map(k -> format(counters.get(k))).collect(Collectors.joining("\t"));
            out.println(keys);
            out.println(values);
        }
        LOGGER.info("a summary of byte code features written to: " + summaryFile.getAbsolutePath());


    }


    static void analyseBytecodeForFeatures(File classFile, Issue.Scope scope, Map<KEYS, Integer> counters,Set<String> classes)  {
        try {
            ClassReader clReader = new ClassReader(new FileInputStream(classFile));
            clReader.accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                    super.visit(version, access, name, signature, superName, interfaces);
                    if ((access & Opcodes.ACC_SYNTHETIC) == 0) {
                        if (scope == Issue.Scope.MAIN) {
                            counters.compute(KEYS.MAIN_CLASSES, (k, v) -> v+1);
                        } else {
                            counters.compute(KEYS.TEST_CLASSES, (k, v) -> v+1);
                        }
                        if (!name.contains("$")) {
                            if (scope == Issue.Scope.MAIN) {
                                counters.compute(KEYS.MAIN_TOPLEVEL_CLASSES, (k, v) -> v+1);
                            } else {
                                counters.compute(KEYS.TEST_TOPLEVEL_CLASSES, (k, v) -> v+1);
                            }
                        }
                        classes.add(name.replace('/', '.'));
                    }

                }

                @Override
                public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                    super.visitField(access, name, descriptor, signature, value);
                    if ((access & Opcodes.ACC_SYNTHETIC) == 0) {
                        if (scope == Issue.Scope.MAIN) {
                            counters.compute(KEYS.MAIN_FIELDS_ALL, (k, v) -> v+1);
                        } else {
                            counters.compute(KEYS.TEST_FIELDS_ALL, (k, v) -> v+1);
                        }
                        if (descriptor.startsWith("L") || descriptor.startsWith("[")) {
                            if (scope == Issue.Scope.MAIN) {
                                counters.compute(KEYS.MAIN_FIELDS_NULLABLE, (k, v) -> v+1);
                            } else {
                                counters.compute(KEYS.TEST_FIELDS_NULLABLE, (k, v) -> v+1);
                            }
                        }
                    }
                    return null;
                }

                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                    super.visitMethod(access, name, descriptor, signature, exceptions);
                    if ((access & Opcodes.ACC_SYNTHETIC) == 0) {
                        if (scope == Issue.Scope.MAIN) {
                            counters.compute(KEYS.MAIN_METHODS_ALL,(k,v) -> v+1);
                        }
                        else {
                            counters.compute(KEYS.TEST_METHODS_ALL,(k,v) -> v+1);
                        }

                        ParsedDescriptor parsedDescriptor = new ParsedDescriptor();
                        parsedDescriptor.parse(descriptor);
                        if (parsedDescriptor.isRefReturnType()) {
                            if (scope == Issue.Scope.MAIN) {
                                counters.compute(KEYS.MAIN_RETURNS,(k,v) -> v+1);
                            }
                            else {
                                counters.compute(KEYS.TEST_RETURNS,(k,v) -> v+1);
                            }
                        }

                        int refParamCount = parsedDescriptor.getNumberOfRefParameters();
                        if (scope == Issue.Scope.MAIN) {
                            counters.compute(KEYS.MAIN_ARGUMENTS,(k,v) -> v+refParamCount);
                        }
                        else {
                            counters.compute(KEYS.TEST_ARGUMENTS,(k,v) -> v+refParamCount);
                        }

                        if (parsedDescriptor.isRefReturnType() || refParamCount>0) {
                            if (scope == Issue.Scope.MAIN) {
                                counters.compute(KEYS.MAIN_METHODS_NULLABLE,(k,v) -> v+1);
                            }
                            else {
                                counters.compute(KEYS.TEST_METHODS_NULLABLE,(k,v) -> v+1);
                            }
                        }
                    }
                    return null;
                }
            },0);
        } catch (IOException e) {
            LOGGER.error("Cannot analyse class file: " + classFile.getAbsolutePath(),e);
        }

    }

    static NumberFormat INT_FORMAT = new DecimalFormat("###,###,###");
    static String format (int number) {
        return INT_FORMAT.format(number);
    }

}
