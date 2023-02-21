package nz.ac.wgtn.nullinference.experiments.spring;

import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.ProjectType;
import nz.ac.wgtn.nullinference.experiments.Utils;
import nz.ac.wgtn.nullinference.experiments.descr.DescriptorParser;
import nz.ac.wgtn.nullinference.experiments.descr.MethodDescriptor;
import org.objectweb.asm.*;
import java.io.*;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static nz.ac.wgtn.nullinference.experiments.spring.Config.*;

/**
 * Analyse the ratio between existing annotations and annotatable elements.
 * @author jens dietrich
 */
public class AnalyseAnnotationRatio extends Experiment {

    public static final File OUTPUT_CSV = new File("experiments-spring/results/rq/annotation-ratio.csv");
    public static final File OUTPUT_LATEX = new File("experiments-spring/results/rq/annotation-ratio.tex");

    public static void main (String[] args) throws Exception {
//        Thread.sleep(10_000);  // time to connect profiler
        new AnalyseAnnotationRatio().analyse();
    }

    int countAnnotatable(String dataName, EnumSet<Issue.IssueType> kinds)  {
        File projectFolder = Config.locateProject(dataName);
        System.out.println("project folder found: " + projectFolder);
        ProjectType projectType = Config.getProjectType(dataName);
        List<File> mainClasses = projectType.getCompiledMainClasses(projectFolder);
        if (mainClasses.isEmpty()) {
            throw new IllegalStateException("No classes found, check whether project has been compiled");
        }
        System.out.println("classes to be analysed: " + mainClasses.size());
        int count = 0;
        for (File classFile:mainClasses) {
            AtomicInteger counter = new AtomicInteger();
            try {
                analyseClassForAnnotatableFeatures(classFile,counter,kinds);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            count = count + counter.get();
        }
        return count;
    }

    int countVoid(String dataName, EnumSet<Issue.IssueType> kinds)  {
        File projectFolder = Config.locateProject(dataName);
        System.out.println("project folder found: " + projectFolder);
        ProjectType projectType = Config.getProjectType(dataName);
        List<File> mainClasses = projectType.getCompiledMainClasses(projectFolder);
        if (mainClasses.isEmpty()) {
            throw new IllegalStateException("No classes found, check whether project has been compiled");
        }
        System.out.println("classes to be analysed: " + mainClasses.size());
        int count = 0;
        for (File classFile:mainClasses) {
            AtomicInteger counter = new AtomicInteger();
            try {
                analyseClassForVoidType(classFile,counter,kinds);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            count = count + counter.get();
        }
        return count;
    }

    private void analyseClassForAnnotatableFeatures(File classFile, AtomicInteger counter, EnumSet<Issue.IssueType> kinds) throws IOException {
        try (InputStream in = new FileInputStream(classFile)) {
            new ClassReader(in).accept(new CollectAnnotatableVisitor(counter, kinds), 0);
        }
    }

    private void analyseClassForVoidType(File classFile, AtomicInteger counter, EnumSet<Issue.IssueType> kinds) throws IOException {
        try (InputStream in = new FileInputStream(classFile)) {
            new ClassReader(in).accept(new CollectVoidVisitor(counter, kinds), 0);
        }
    }

    static final Set<String> PRIMITIVE_TYPES = Set.of(
        "int","boolean","char","byte","short","float","long","double","void"
    );

    static class CollectAnnotatableVisitor extends ClassVisitor {
        private AtomicInteger counter = null;
        private EnumSet<Issue.IssueType> kinds = null;
        public CollectAnnotatableVisitor(AtomicInteger counter,EnumSet<Issue.IssueType> kinds) {
            super(Opcodes.ASM9);
            this.counter = counter;
            this.kinds = kinds;
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            if (kinds.contains(Issue.IssueType.FIELD) && (access & Opcodes.ACC_SYNTHETIC) != 1) {
                String type = DescriptorParser.parseType(descriptor);
                if (!PRIMITIVE_TYPES.contains(type)) {
                    counter.incrementAndGet();
                }
            }
            return null;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            if ((kinds.contains(Issue.IssueType.ARGUMENT) || kinds.contains(Issue.IssueType.RETURN_VALUE)) && (access & Opcodes.ACC_SYNTHETIC) != 1) {
                MethodDescriptor methodDescriptor = DescriptorParser.parseMethodDescriptor(descriptor);
                if (kinds.contains(Issue.IssueType.RETURN_VALUE) && !PRIMITIVE_TYPES.contains(methodDescriptor.getReturnType())) {
                    counter.incrementAndGet();
                }
                if (kinds.contains(Issue.IssueType.ARGUMENT)) {
                    for (String paramType:methodDescriptor.getParamTypes()) {
                        if (!PRIMITIVE_TYPES.contains(paramType)) {
                            counter.incrementAndGet();
                        }
                    }
                }
            }
            return null;
        }
    }


    // collects occurances of Void -- Void cannot be instantiated, and can therefore only be null
    static class CollectVoidVisitor extends ClassVisitor {
        private AtomicInteger counter = null;
        private EnumSet<Issue.IssueType> kinds = null;
        private String VOID = Void.class.getName();
        public CollectVoidVisitor(AtomicInteger counter,EnumSet<Issue.IssueType> kinds) {
            super(Opcodes.ASM9);
            this.counter = counter;
            this.kinds = kinds;
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            if (kinds.contains(Issue.IssueType.FIELD) && (access & Opcodes.ACC_SYNTHETIC) != 1) {
                String type = DescriptorParser.parseType(descriptor);
                if (type.equals(VOID)) {
                    counter.incrementAndGet();
                }
            }
            return null;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            if ((kinds.contains(Issue.IssueType.ARGUMENT) || kinds.contains(Issue.IssueType.RETURN_VALUE)) && (access & Opcodes.ACC_SYNTHETIC) != 1) {
                MethodDescriptor methodDescriptor = DescriptorParser.parseMethodDescriptor(descriptor);
                if (kinds.contains(Issue.IssueType.RETURN_VALUE) && methodDescriptor.getReturnType().equals(VOID)) {
                    counter.incrementAndGet();
                }
                if (kinds.contains(Issue.IssueType.ARGUMENT)) {
                    for (String paramType:methodDescriptor.getParamTypes()) {
                        if (paramType.equals(VOID)) {
                            counter.incrementAndGet();
                        }
                    }
                }
            }
            return null;
        }
    }


    public void analyse()  {

        Column[] columns = new Column[] {
            Column.First,
            new Column() {
                @Override public String name() {
                    return "annotated";
                }
                @Override public String value(String dataName) {
                    return Utils.format(countIssues(EXTRACTED_ISSUES_FOLDER,dataName,true));
                }
            },
            new Column() {
                @Override public String name() {
                    return "annotatable";
                }
                @Override public String value(String dataName) {
                    return Utils.format(countAnnotatable(dataName, EnumSet.allOf(Issue.IssueType.class)));
                }
            },
            new Column() {
                @Override public String name() {
                    return "annotation ratio";
                }
                @Override public String value(String dataName) {
                    int count1 = countIssues(EXTRACTED_ISSUES_FOLDER,dataName,true);
                    int count2 = countAnnotatable(dataName,EnumSet.allOf(Issue.IssueType.class));
                    return Utils.format(((double)count1)/((double)count2));
                }
            },
            new Column() {
                @Override public String name() {
                    return "\\texttt{Void} usage";
                }
                @Override public String value(String dataName) {
                    return Utils.format(countVoid(dataName, EnumSet.allOf(Issue.IssueType.class)));
                }
            }
        };


        TableGenerator csvOutput = new CSVTableGenerator(OUTPUT_CSV);
        TableGenerator latexOutput = new LatexTableGenerator(OUTPUT_LATEX,"|lrrrr|");

        this.run(FULL_DATASET,"Annotated vs annotatable program elements, in the last column the number of annotatable elements of type \\texttt{java.lang.Void} is reported","tab:annotation-ratio",columns,csvOutput,latexOutput);
    }

}
