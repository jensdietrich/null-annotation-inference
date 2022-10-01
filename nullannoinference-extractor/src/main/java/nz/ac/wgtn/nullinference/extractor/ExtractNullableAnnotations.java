package nz.ac.wgtn.nullinference.extractor;

import com.google.common.base.Preconditions;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.ProjectType;
import org.objectweb.asm.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;

/**
 * Script to analyse the use of nullable / nonnull annotations in bytecode.
 * NOTE: this does not scan test classes.
 * @author jens dietrich
 */
public class ExtractNullableAnnotations {

    // should catch all null-related annotations, and possibly more ..
    public static Predicate<String> CATCH_ANY_NULL_ANNOTATION = descriptor -> descriptor.toLowerCase().contains("null");

    // additional property keys for the issues extracted
    public static final String PROPERTY_NULLABLE_ANNOTATION_TYPE = "nullable-annotation-type";
    public static final String PROPERTY_ANNOTATED_CODE_LANGUAGE = "annotated-code-implementation-language";

    public static Set<Issue> findNullAnnotated(ProjectType project, File projectFolder) {
        // default filter -- look for all annotations containing "Nullable"
        Predicate<String> filter = n -> n.toLowerCase().contains("nullable");
        return findNullAnnotated(project,projectFolder,filter);
    }
    public static Set<Issue> findNullAnnotated(ProjectType project, File projectFolder, Predicate<String> isDescriptorForNullAnnotation) {

        Preconditions.checkArgument(projectFolder.exists(),projectFolder.getAbsolutePath() + " must exist");
        Preconditions.checkArgument(projectFolder.isDirectory(),projectFolder.getAbsolutePath() + " must be a folder");
        project.checkProjectRootFolder(projectFolder);

        Collection<File> classFiles = project.getCompiledMainClasses(projectFolder);
        Set<Issue> issues = new HashSet<>();
        for (File classFile:classFiles) {
            String implementationLanguage = project.getImplementationLanguage(classFile);
            try (InputStream in = new FileInputStream(classFile)) {
                new ClassReader(in).accept(new AnnotationReporter(issues,isDescriptorForNullAnnotation,implementationLanguage), 0);
            }
            catch (Exception x) {
                x.printStackTrace();
            }
        }
        return issues;
    }

    static class AnnotationReporter extends ClassVisitor {
        Set<Issue> issues = null;
        String currentClassName = null;
        String currentMethodName = null;
        String currentMethodDescriptor = null;
        String currentFieldName = null;
        String currentFieldDescriptor = null;
        Predicate<String> isNullAnnotation = null;
        String implementationLanguage = null;

        public AnnotationReporter(Set<Issue> issues,Predicate<String> isNullAnnotation,String implementationLanguage) {
            super(Opcodes.ASM9);
            this.issues = issues;
            this.isNullAnnotation = isNullAnnotation;
            this.implementationLanguage = implementationLanguage;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            this.currentClassName = name.replace('/','.');
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            this.currentMethodName = name;
            if (name.equals("<init>")) {
                // special rule to make this consistent with how instrumentation records constructors
                this.currentMethodDescriptor = descriptor.replace(")V",")");
            }
            else {
                this.currentMethodDescriptor = descriptor;
            }
            return new MethodVisitor(Opcodes.ASM9) {
                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    if (isNullAnnotation.test(descriptor)) {
                        Issue issue = new Issue(currentClassName, currentMethodName, currentMethodDescriptor,null, Issue.IssueType.RETURN_VALUE);
                        setupIssue(issue,descriptor);
                        issues.add(issue);
                    }
                    return super.visitAnnotation(descriptor, visible);
                }

                @Override
                public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
                    if (isNullAnnotation.test(descriptor)) {
                        Issue issue = new Issue(currentClassName, currentMethodName, currentMethodDescriptor,null, Issue.IssueType.ARGUMENT,parameter);
                        setupIssue(issue,descriptor);
                        issues.add(issue);
                    }
                    return super.visitAnnotation(descriptor, visible);                }
            };
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            this.currentFieldName = name;
            this.currentFieldDescriptor = descriptor;
            return new FieldVisitor(Opcodes.ASM9) {
                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    if (isNullAnnotation.test(descriptor)) {
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
            issue.setProperty(PROPERTY_NULLABLE_ANNOTATION_TYPE,convertReftypeBytecode2Sourcecoderepresentation(descriptor));
            issue.setProperty(PROPERTY_ANNOTATED_CODE_LANGUAGE,implementationLanguage);
            issue.setScope(Issue.Scope.MAIN);
        }

        private String convertReftypeBytecode2Sourcecoderepresentation(String s) {
            assert s.startsWith("L");
            assert s.endsWith(";");
            return s.substring(1,s.length()-1).replace('/','.');
        }
    }

}

