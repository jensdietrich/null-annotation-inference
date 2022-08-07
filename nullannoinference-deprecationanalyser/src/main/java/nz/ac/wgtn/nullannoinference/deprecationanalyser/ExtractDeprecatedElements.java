package nz.ac.wgtn.nullannoinference.deprecationanalyser;

import com.google.common.base.Preconditions;
import nz.ac.wgtn.nullannoinference.commons.ProjectType;
import org.objectweb.asm.*;
import java.io.*;
import java.util.*;

/**
 * Extracts elements annotated with @java.lang.Deprecated and creates test file with deprecated elements.
 * Each line is an element, the format is classname [::membername descriptor]  -- member information
 * is only includes for deprecated methods and fields (classes can also be deprecated).
 * @author jens dietrich
 */
public class ExtractDeprecatedElements {



    public static void run  (ProjectType projectType, File projectRootFolder, File outputFile) throws IOException {
        Preconditions.checkArgument(projectRootFolder.exists(),projectRootFolder.getAbsolutePath() + " must exist");
        Preconditions.checkArgument(projectRootFolder.isDirectory(),projectRootFolder.getAbsolutePath() + " must be a folder");

        Main.LOGGER.info("Analyse project for deprecated elements " + projectRootFolder.getAbsolutePath());
        List<String> deprecatedElements = findDeprecatedElements(projectType,projectRootFolder);

        if (!outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }

        try (PrintWriter out = new PrintWriter(new FileWriter(outputFile))) {
            for (String record:deprecatedElements) {
                out.println(record);
            }
        }

        Main.LOGGER.info("Analysis results written to " + outputFile.getAbsolutePath());
        Main.LOGGER.info("\t"+deprecatedElements.size()+" elements identified");


    }

    static List<String> findDeprecatedElements(ProjectType projectType, File folder) throws IOException {
        Collection<File> classFiles = projectType.getCompiledTestClasses(folder);
        if (classFiles.isEmpty()) {
            throw new IllegalStateException("No .class files found, make sure that the project has been built");
        }
        List<String> methods = new ArrayList<>();
        for (File classFile:classFiles) {
            // System.out.println("Analysing: " + classFile);
            try (InputStream in = new FileInputStream(classFile)) {
                new ClassReader(in).accept(new NegativeTestFinder(methods), 0);
            }
        }
        return methods;
    }

    static class NegativeTestFinder extends ClassVisitor {
        public static final String DEPRECATED_TYPE = "Ljava/lang/Deprecated;";
        List<String> deprecatedElements = null;
        String currentClass = null;
        String currentElement = null;
        public NegativeTestFinder(List<String> deprecatedElements) {
            super(Opcodes.ASM9);
            this.deprecatedElements = deprecatedElements;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            this.currentClass = name.replace('/','.');
        }

        @Override public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if (DEPRECATED_TYPE.equals(descriptor)) {
                // class level annotation
                deprecatedElements.add(currentClass);
            }
            return null;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            return new MethodVisitor(Opcodes.ASM9) {
                private String currentElement = name;
                private String currentDescriptor = descriptor;
                @Override public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    if (DEPRECATED_TYPE.equals(descriptor)) {
                        deprecatedElements.add(currentClass + "::" + currentElement + currentDescriptor);
                    }
                    return null;
                }
            };
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            return new FieldVisitor(Opcodes.ASM9) {
                private String currentElement = name;
                private String currentDescriptor = descriptor;
                @Override public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    if (DEPRECATED_TYPE.equals(descriptor)) {
                        deprecatedElements.add(currentClass + "::" + currentElement + currentDescriptor);
                    }
                    return null;
                }
            };
        }
    }
}
