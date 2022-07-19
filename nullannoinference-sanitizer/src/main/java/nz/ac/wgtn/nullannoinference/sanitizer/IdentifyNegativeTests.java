package nz.ac.wgtn.nullannoinference.sanitizer;

import com.google.common.base.Preconditions;
import nz.ac.wgtn.nullannoinference.commons.ProjectType;
import org.objectweb.asm.*;
import java.io.*;
import java.util.*;

/**
 * Script to analyse projects for the presence of negative tests.
 * Will produce a csv file (\t-separated) containing of class name, test method name (no descriptor, assuming that test methods are not overloaded).
 * @author jens dietrich
 */
public class IdentifyNegativeTests {

    public static final String CSV_SEP = "\t";

    public static void run  (ProjectType projectType, File projectRootFolder, File outputFile) throws IOException {
        Preconditions.checkArgument(projectRootFolder.exists(),projectRootFolder.getAbsolutePath() + " must exist");
        Preconditions.checkArgument(projectRootFolder.isDirectory(),projectRootFolder.getAbsolutePath() + " must be a folder");

        SantitiseObservedIssues.LOGGER.info("Analysis project for negative tests " + projectRootFolder.getAbsolutePath());

        Set<MethodInfo> methods = new TreeSet<>();
        methods.addAll(findNegativeTests(projectType,projectRootFolder));

        if (!outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }

        try (PrintWriter out = new PrintWriter(new FileWriter(outputFile))) {
            for (MethodInfo m:methods) {
                out.print(m.getClassName());
                out.print(CSV_SEP);
                out.print(m.getName());
                out.print(CSV_SEP);
                out.print(m.getDescriptor());
                out.print(CSV_SEP);
                out.println();
            }
        }

        SantitiseObservedIssues.LOGGER.info("Analysis results written to " + outputFile.getAbsolutePath());


    }

    static Set<MethodInfo> findNegativeTests(ProjectType projectType, File folder) throws IOException {
        Collection<File> classFiles = projectType.getCompiledTestClasses(folder);
        if (classFiles.isEmpty()) {
            throw new IllegalStateException("No .class files found, make sure that the project has been built");
        }
        Set<MethodInfo> methods = new HashSet<>();
        for (File classFile:classFiles) {
            // System.out.println("Analysing: " + classFile);
            try (InputStream in = new FileInputStream(classFile)) {
                new ClassReader(in).accept(new NegativeTestFinder(methods), 0);
            }
        }
        return methods;
    }

    static class NegativeTestFinder extends ClassVisitor {
        Set<MethodInfo> methods = null;
        String currentClass = null;
        public NegativeTestFinder(Set<MethodInfo> methods) {
            super(Opcodes.ASM9);
            this.methods = methods;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            this.currentClass = name.replace('/','.');
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            return new MethodVisitor(Opcodes.ASM9) {
                private String currentClassName = name;
                private String currentDescriptor = descriptor;
                private boolean isJunit4TestAnnotated = false;

                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                    // method is overloaded -- catch all versions by ignoring descriptor
                    if ("org/junit/jupiter/api/Assertions".equals(owner) && "assertThrows".equals(name)) {
                        methods.add(new MethodInfo(currentClass,currentClassName,currentDescriptor));
                    }

                    // add some third-party assertj callsites -- those are used in spring
                    if ("org/assertj/core/api/ThrowableAssert".equals(owner) || "org/assertj/core/api/AbstractThrowableAssert".equals(owner)) {
                        methods.add(new MethodInfo(currentClass,currentClassName,currentDescriptor));
                    }
                }

                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    isJunit4TestAnnotated = "Lorg/junit/Test;".equals(descriptor);
                    return new AnnotationVisitor(Opcodes.ASM9) {
                        @Override
                        public void visit(String name, Object value) {
                            super.visit(name, value);
                            if (isJunit4TestAnnotated && "expected".equals(name)) {
                                methods.add(new MethodInfo(currentClass,currentClassName,currentDescriptor));
                            }
                        }
                    };
                }
            };
        }
    }
}
