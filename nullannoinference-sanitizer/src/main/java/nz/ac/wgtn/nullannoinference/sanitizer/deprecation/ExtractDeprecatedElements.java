package nz.ac.wgtn.nullannoinference.sanitizer.deprecation;

import com.google.common.base.Preconditions;
import nz.ac.wgtn.nullannoinference.commons.ProjectType;
import nz.ac.wgtn.nullannoinference.sanitizer.LogSystem;
import org.apache.logging.log4j.Logger;
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

    public static final Logger LOGGER = LogSystem.getLogger("deprecation-analyses");

    static void dumpDeprecatedElements(List<String> deprecatedElements, File outputFile) {
        try (PrintWriter out = new PrintWriter(new FileWriter(outputFile))) {
            for (String record:deprecatedElements) {
                out.println(record);
            }
            LOGGER.info("Analysis results written to " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Error writing deprecated elements to " + outputFile.getAbsolutePath(),e);
        }
    }

    static List<String> findDeprecatedElements(ProjectType projectType, File folder,File outputFile) throws IOException {
        Collection<File> classFiles = projectType.getCompiledMainClasses(folder);
        return findDeprecatedElements(classFiles,outputFile);
    }

    static List<String> findDeprecatedElements(Collection<File> classFiles,File outputFile) throws IOException {
        Preconditions.checkArgument(!classFiles.isEmpty(),"No .class files found, make sure that the project has been built");
        List<String> elements = new ArrayList<>();
        for (File classFile:classFiles) {
            // System.out.println("Analysing: " + classFile);
            try (InputStream in = new FileInputStream(classFile)) {
                new ClassReader(in).accept(new DeprecatedElementFinder(elements), 0);
            }
        }
        if (outputFile!=null) {
            dumpDeprecatedElements(elements,outputFile);
        }
        LOGGER.info("\t"+elements.size()+" elements identified");
        return elements;
    }

    static class DeprecatedElementFinder extends ClassVisitor {
        public static final String DEPRECATED_TYPE = "Ljava/lang/Deprecated;";
        List<String> deprecatedElements = null;
        String currentClass = null;
        public DeprecatedElementFinder(List<String> deprecatedElements) {
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
