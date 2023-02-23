package nz.ac.wgtn.nullinference.extractor;

import com.google.common.base.Preconditions;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.ProjectType;
import org.objectweb.asm.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Script to analyse the use java.lang.Void in bytecode.
 * Void can be considered as implicitely annotated with null.
 * NOTE: this does not scan test classes.
 * @author jens dietrich
 */
public class ExtractVoid {

    // should catch all null-related annotations, and possibly more ..
    public static Predicate<String> CATCH_ANY_NULL_ANNOTATION = descriptor -> descriptor.toLowerCase().contains("null");

    // additional property keys for the issues extracted
    public static final String PROPERTY_NULLABLE_ANNOTATION_TYPE = "nullable-annotation-type";
    public static final String PROPERTY_ANNOTATED_CODE_LANGUAGE = "annotated-code-implementation-language";


    public static Set<Issue> findNullAnnotated(ProjectType project, File projectFolder) {

        Preconditions.checkArgument(projectFolder.exists(),projectFolder.getAbsolutePath() + " must exist");
        Preconditions.checkArgument(projectFolder.isDirectory(),projectFolder.getAbsolutePath() + " must be a folder");
        project.checkProjectRootFolder(projectFolder);

        Collection<File> classFiles = project.getCompiledMainClasses(projectFolder);
        Set<Issue> issues = new HashSet<>();
        for (File classFile:classFiles) {
            String implementationLanguage = project.getImplementationLanguage(classFile);
            try (InputStream in = new FileInputStream(classFile)) {
                new ClassReader(in).accept(new VoidReporter(issues,implementationLanguage), 0);
            }
            catch (Exception x) {
                x.printStackTrace();
            }
        }
        return issues;
    }

    static class VoidReporter extends ClassVisitor {
        Set<Issue> issues = null;
        String currentClassName = null;
        String implementationLanguage = null;

        static Type VOID = Type.getType(Void.class);

        public VoidReporter(Set<Issue> issues, String implementationLanguage) {
            super(Opcodes.ASM9);
            this.issues = issues;
            this.implementationLanguage = implementationLanguage;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            this.currentClassName = name.replace('/','.');
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            String descriptor2 = descriptor;
            if (name.equals("<init>")) {
                // special rule to make this consistent with how instrumentation records constructors
                descriptor2 = descriptor.replace(")V",")");
            }


            Type[] argTypes = Type.getArgumentTypes(descriptor);
            Type retType = Type.getReturnType(descriptor);

            if (VOID.equals(retType)) {
                Issue issue = new Issue(currentClassName, name, descriptor2,null, Issue.IssueType.RETURN_VALUE);
                setupIssue(issue);
                issues.add(issue);
            }

            for (int i=0;i<argTypes.length;i++) {
                if (VOID.equals(argTypes[i])) {
                    Issue issue = new Issue(currentClassName, name, descriptor2,null, Issue.IssueType.ARGUMENT,i);
                    setupIssue(issue);
                    issues.add(issue);
                }
            }
            return null;
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            Type fType = Type.getType(descriptor);
            if (VOID.equals(fType)) {
                Issue issue = new Issue(currentClassName, name, descriptor,null, Issue.IssueType.FIELD);
                setupIssue(issue);
                issues.add(issue);
            }
            return null;
        }

        private void setupIssue(Issue issue) {
            issue.setProvenanceType(Issue.ProvenanceType.EXTRACTED);
            issue.setProperty(PROPERTY_NULLABLE_ANNOTATION_TYPE,Void.class.getTypeName());
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

