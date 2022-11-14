import org.objectweb.asm.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Script to analyse the use of nullable annotations in bytecode.
 * It analyses code with @NonNull annotations.
 * @author jens dietrich
 */
public class ExtractNonNullAnnotations {

    public static void main (String[] args) {
        if (args.length<2) {
            throw new IllegalArgumentException("Two arguments expected -- a folder containing a built maven project, and the name of an output file");
        }
        File projectFolder = new File(args[0]);
        File outputFile = new File(args[1]);

        Set<Issue> issues = findNonNullAnnotated(projectFolder);
        IssuePersistency.save(issues,outputFile);

    }

    public static Set<Issue> findNonNullAnnotated(File projectFolder) {

        File binClassFolder = new File(projectFolder,"target/classes");
        Set<File> classFiles = collectJavaFiles(binClassFolder);
        Set<Issue> antiIssues = new HashSet<>();
        for (File classFile:classFiles) {
            try (InputStream in = new FileInputStream(classFile)) {
                new ClassReader(in).accept(new AnnotationReporter(antiIssues), 0);
            }
            catch (Exception x) {
                x.printStackTrace();
            }
        }


        return antiIssues;
    }

    static class AnnotationReporter extends ClassVisitor {
        Set<Issue> issues = null;
        String currentClassName = null;
        String currentMethodName = null;
        String currentMethodDescriptor = null;
        String currentFieldName = null;
        String currentFieldDescriptor = null;

        public AnnotationReporter(Set<Issue> issues) {
            super(Opcodes.ASM5);
            this.issues = issues;
        }

        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.currentClassName = name.replace('/','.');
        }

        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            this.currentMethodName = name;
            if (name.equals("<init>")) {
                // special rule to make this consistent with how instrumentation records constructors
                this.currentMethodDescriptor = descriptor.replace(")V",")");
            }
            else {
                this.currentMethodDescriptor = descriptor;
            }
            return new MethodVisitor(Opcodes.ASM5) {
                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    if (isNonNullAnnotation(descriptor)) {
                        Issue issue = new Issue(currentClassName, currentMethodName, currentMethodDescriptor,null, Issue.IssueType.RETURN_VALUE);
                        setupIssue(issue,descriptor);
                        issues.add(issue);
                    }
                    return super.visitAnnotation(descriptor, visible);
                }

                @Override
                public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
                    if (isNonNullAnnotation(descriptor)) {
                        Issue issue = new Issue(currentClassName, currentMethodName, currentMethodDescriptor,null, Issue.IssueType.ARGUMENT,parameter);
                        setupIssue(issue,descriptor);
                        issues.add(issue);
                    }
                    return super.visitAnnotation(descriptor, visible);
                }


            };
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            this.currentFieldName = name;
            this.currentFieldDescriptor = descriptor;
            return new FieldVisitor(Opcodes.ASM5) {
                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    if (isNonNullAnnotation(descriptor)) {
                        Issue issue = new Issue(currentClassName, currentFieldName, currentFieldDescriptor,null, Issue.IssueType.FIELD);
                        setupIssue(issue,descriptor);
                        issues.add(issue);
                    }
                    return super.visitAnnotation(descriptor, visible);
                }
            };
        }

        private void setupIssue(Issue issue, String descriptor) {
            issue.setProvenanceType(Issue.ProvenanceType.EXTRACTED);
            issue.setScope(Issue.Scope.MAIN);
        }

    }

    static Set<File> collectJavaFiles(File folder) {
        Set<File> files = new HashSet<>();
        collectJavaFiles(folder,files);
        return files;
    }

    private static void collectJavaFiles(File folder,Set<File> collector)  {
        for (File f:folder.listFiles()) {
            if (f.isDirectory()) {
                collectJavaFiles(f,collector);
            }
            else if (f.getName().endsWith(".class") && !f.getName().equals("package-info.class")) {
                collector.add(f);
            }
        }
    }

    private static boolean isNonNullAnnotation(String descriptor) {
        return descriptor.toLowerCase().contains("nonnull");
    }

}

