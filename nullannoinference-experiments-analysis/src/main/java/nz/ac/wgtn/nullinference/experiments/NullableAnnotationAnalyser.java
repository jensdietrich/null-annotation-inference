package nz.ac.wgtn.nullinference.experiments;

import com.google.common.base.Preconditions;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.Project;
import org.objectweb.asm.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;

/**
 * Script to analyse the use of nullable / nonnull annotations in bytecode.
 * NOTE: this does not scan test classes.
 * Returns a string representation of null-annotated program elements.
 * @author jens dietrich
 */
public class NullableAnnotationAnalyser {
    
    // should catch all null-related annotations, and possibly more ..
    public static Predicate<String> CATCH_ANY_NULL_ANNOTATION = descriptor -> descriptor.toLowerCase().contains("null");

    public static Set<Issue> findNullAnnotated(Project project, File projectFolder, Predicate<String> isDescriptorForNullAnnotation) {

        Preconditions.checkArgument(projectFolder.exists(),projectFolder.getAbsolutePath() + " must exist");
        Preconditions.checkArgument(projectFolder.isDirectory(),projectFolder.getAbsolutePath() + " must be a folder");
        project.checkProjectRootFolder(projectFolder);

        Collection<File> classFiles = project.getCompiledMainClasses(projectFolder);
        Set<Issue> issues = new HashSet<>();
        for (File classFile:classFiles) {
            try (InputStream in = new FileInputStream(classFile)) {
                new ClassReader(in).accept(new AnnotationReporter(issues,isDescriptorForNullAnnotation), 0);
            }
            catch (Exception x) {
                x.printStackTrace();
            }
        }
        return issues;
    }

    static void increaseCount(Map<String,Integer> counts,String key) {
        counts.compute(key,(k,v) -> v==null?1:(1+v));
    }

    static class AnnotationReporter extends ClassVisitor {
        Set<Issue> issues = null;
        String currentClassName = null;
        String currentMethodName = null;
        String currentMethodDescriptor = null;
        String currentFieldName = null;
        String currentFieldDescriptor = null;
        Predicate<String> isNullAnnotation = null;
        public AnnotationReporter(Set<Issue> issues,Predicate<String> isNullAnnotation) {
            super(Opcodes.ASM9);
            this.issues = issues;
            this.isNullAnnotation = isNullAnnotation;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            this.currentClassName = name.replace('/','.');
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            this.currentMethodName = name;
            this.currentMethodDescriptor = descriptor;
            return new MethodVisitor(Opcodes.ASM9) {
                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    if (isNullAnnotation.test(descriptor)) {
                        Issue issue = new Issue(currentClassName, currentMethodName, currentMethodDescriptor,null, Issue.IssueType.RETURN_VALUE);
                        issues.add(issue);
                    }
                    return super.visitAnnotation(descriptor, visible);
                }

                @Override
                public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
                    if (isNullAnnotation.test(descriptor)) {
                        Issue issue = new Issue(currentClassName, currentMethodName, currentMethodDescriptor,null, Issue.IssueType.ARGUMENT,parameter);
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
                        issues.add(issue);
                    }
                    return super.visitAnnotation(descriptor, visible);
                }
            };
        }
    }

}

