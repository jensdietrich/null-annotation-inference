package nz.ac.wgtn.nullannoinference.sanitizer.negtests;

import nz.ac.wgtn.nullannoinference.commons.ProjectType;
import nz.ac.wgtn.nullannoinference.sanitizer.LogSystem;
import org.apache.logging.log4j.Logger;
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
    public static final Logger LOGGER = LogSystem.getLogger("negative-test-analysis");


    public static void main(String[] args) throws Exception {
        findNegativeTests(
            ProjectType.ERROR_PRONE,
            new File("/Users/jens/Development/null-annotation-inference/experiments-additional/projects/original/error-prone"),
            new File("/Users/jens/Development/null-annotation-inference/experiments-additional/tmp/neg-tests-erroprone.txt")
        );
    }

    static void dumpNegativeTests(Collection<MethodInfo> negativeTests,File outputFile) {
        try (PrintWriter out = new PrintWriter(new FileWriter(outputFile))) {
            for (MethodInfo m:negativeTests) {
                out.print(m.getClassName());
                out.print(CSV_SEP);
                out.print(m.getName());
                out.print(CSV_SEP);
                out.print(m.getDescriptor());
                out.print(CSV_SEP);
                out.println();
            }
        } catch (IOException x) {
            LOGGER.error("error writting negative tests to " + outputFile.getAbsolutePath(),x);
        }

        LOGGER.info("Analysis results written to " + outputFile.getAbsolutePath());
        LOGGER.info("\t"+negativeTests.size()+" negative tests identified");
    }

    static Set<MethodInfo> findNegativeTests(ProjectType projectType, File folder,File outputFile) throws IOException {
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

        if (outputFile!=null) {
            dumpNegativeTests(methods,outputFile);
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
                private String currentMethodName = name;
                private String currentDescriptor = descriptor;
                private boolean isJunit4TestAnnotated = false;

                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                    // method is overloaded -- catch all versions by ignoring descriptor
                    if ("org/junit/jupiter/api/Assertions".equals(owner) && "assertThrows".equals(name)) {
                        methods.add(new MethodInfo(currentClass,currentMethodName,currentDescriptor));
                    }

                    // this was already defined in junit4 !
                    if ("org/junit/Assert".equals(owner) && "assertThrows".equals(name)) {
                        methods.add(new MethodInfo(currentClass,currentMethodName,currentDescriptor));
                    }

                    // add some third-party assertj callsites -- those are used in spring
                    if ("org/assertj/core/api/Assertions".equals(owner)) {
                        if (name.startsWith("assertThat") && name.endsWith("Exception") && !name.equals("assertThatNoException")) {
                            methods.add(new MethodInfo(currentClass,currentMethodName,currentDescriptor));
                        }
                        else if (name.startsWith("assertThatThrownBy")) {
                            methods.add(new MethodInfo(currentClass,currentMethodName,currentDescriptor));
                        }
                    }
                    if (owner.startsWith("org/assertj/core/api/") && owner.contains("Throwable") && owner.contains("Assert")) {
                        methods.add(new MethodInfo(currentClass,currentMethodName,currentDescriptor));
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
                                methods.add(new MethodInfo(currentClass,currentMethodName,currentDescriptor));
                            }
                        }
                    };
                }
            };
        }
    }
}
