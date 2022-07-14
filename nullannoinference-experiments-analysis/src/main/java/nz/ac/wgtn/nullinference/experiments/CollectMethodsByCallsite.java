package nz.ac.wgtn.nullinference.experiments;

import com.google.common.base.Preconditions;
import nz.ac.wgtn.nullannoinference.commons.ProjectType;
import org.objectweb.asm.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Script to collect methods (as strings -- classname::methodName descriptor) if they contain a certain callsite.
 * @author jens dietrich
 */
public class CollectMethodsByCallsite {

    public interface CallsitePredicate {
        boolean test(String klass, String method, String descriptor);
    }

    public static Set<String> findMethodsWithCallsites(ProjectType project, File projectFolder, CallsitePredicate filter) {

        Preconditions.checkArgument(projectFolder.exists(),projectFolder.getAbsolutePath() + " must exist");
        Preconditions.checkArgument(projectFolder.isDirectory(),projectFolder.getAbsolutePath() + " must be a folder");
        project.checkProjectRootFolder(projectFolder);

        Collection<File> classFiles = project.getCompiledMainClasses(projectFolder);
        Set<String> methods = new HashSet<>();
        for (File classFile:classFiles) {
            try (InputStream in = new FileInputStream(classFile)) {
                new ClassReader(in).accept(new AnnotationReporter(methods,filter), 0);
            }
            catch (Exception x) {
                x.printStackTrace();
            }
        }
        return methods;
    }

    static class AnnotationReporter extends ClassVisitor {
        Set<String> methodsWithCallsites = null;
        String currentClassName = null;
        String currentMethodName = null;
        String currentMethodDescriptor = null;
        CallsitePredicate filter = null;
        public AnnotationReporter(Set<String> methodsWithCallsites,CallsitePredicate filter) {
            super(Opcodes.ASM9);
            this.methodsWithCallsites = methodsWithCallsites;
            this.filter = filter;
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
                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                    if (filter.test(owner,name,descriptor)) {
                        String method = currentClassName.replace('/','.')+"::"+currentMethodName+currentMethodDescriptor;
                        methodsWithCallsites.add(method);
                    }
                }
            };
        }
    }

}

