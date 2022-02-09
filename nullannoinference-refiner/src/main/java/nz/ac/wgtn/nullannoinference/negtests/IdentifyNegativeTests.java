package nz.ac.wgtn.nullannoinference.negtests;

import com.google.common.base.Preconditions;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.*;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Script to analyse projects for thee presence of negative tests.
 * Will produce a csv file (\t-separated) containing of class name, test method name (no descriptor, assuming that test methods are not overloaded).
 * @author jens dietrich
 */
public class IdentifyNegativeTests {

    public static final String CSV_SEP = "\t";

    public static void main (String[] args) throws IOException {

        Preconditions.checkArgument(args.length==2,"two arguments required -- the root folder of folders containing poms, and the name of the output file (csv)");
        File rootFolder = new File(args[0]);
        Preconditions.checkArgument(rootFolder.exists(),rootFolder.getAbsolutePath() + " must exist");
        Preconditions.checkArgument(rootFolder.isDirectory(),rootFolder.getAbsolutePath() + " must be a folder");
        File output = new File(args[1]);

        Set<Method> methods = new TreeSet<>();

        for (File program:rootFolder.listFiles(f -> f.isDirectory())) {
            String name = program.getName();
            System.out.println("analysing: " + name);
            methods.addAll(findNegativeTests(program));
        }

        try (PrintWriter out = new PrintWriter(new FileWriter(output))) {
            for (Method m:methods) {
                out.print(m.getClassName());
                out.print(CSV_SEP);
                out.print(m.getName());
                out.print(CSV_SEP);
                out.print(m.getDescriptor());
                out.print(CSV_SEP);
                out.println();
            }
        }

        System.out.println("Analysis results written to " + output.getAbsolutePath());


    }

    static Set<Method> findNegativeTests(File project) throws IOException {
        File compiledTestClasses = new File(project,"target/test-classes");
        if (!compiledTestClasses.exists()) {
            throw new IllegalStateException("project must be built before analysis can be found (mvn test, or mvn test-compile)");
        }
        Collection<File> classFiles = FileUtils.listFiles(compiledTestClasses,new String[]{"class"},true);
        if (classFiles.isEmpty()) {
            throw new IllegalStateException("No .class files found, make sure that the project has been built");
        }
        Set<Method> methods = new HashSet<>();
        for (File classFile:FileUtils.listFiles(project,new String[]{"class"},true)) {
            // System.out.println("Analysing: " + classFile);
            try (InputStream in = new FileInputStream(classFile)) {
                new ClassReader(in).accept(new NegativeTestFinder(methods), 0);
            }
        }
        return methods;
    }

    static class NegativeTestFinder extends ClassVisitor {
        Set<Method> methods = null;
        String currentClass = null;
        public NegativeTestFinder(Set<Method> methods) {
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
                        methods.add(new Method(currentClass,currentClassName,currentDescriptor));
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
                                methods.add(new Method(currentClass,currentClassName,currentDescriptor));
                            }
                        }
                    };
                }
            };
        }
    }
}
