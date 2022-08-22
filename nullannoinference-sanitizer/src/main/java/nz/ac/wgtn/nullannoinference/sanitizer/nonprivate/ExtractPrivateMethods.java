package nz.ac.wgtn.nullannoinference.sanitizer.nonprivate;

import com.google.common.base.Preconditions;
import nz.ac.wgtn.nullannoinference.commons.ProjectType;
import nz.ac.wgtn.nullannoinference.sanitizer.LogSystem;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.*;
import java.io.*;
import java.util.*;

/**
 * Extracts private methods, incl package visible.
 * @author jens dietrich
 */
public class ExtractPrivateMethods {

    public static final Logger LOGGER = LogSystem.getLogger("visibility-analyses");

    static void dumpPrivateMethods(Set<String> privateMethods, File outputFile) {
        try (PrintWriter out = new PrintWriter(new FileWriter(outputFile))) {
            for (String record:privateMethods) {
                out.println(record);
            }
            LOGGER.info("analysis results written to " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("error writing private elements to " + outputFile.getAbsolutePath(),e);
        }
    }

    static Set<String> findPrivateMethods(ProjectType projectType, File folder, File outputFile) throws IOException {
        Collection<File> classFiles = projectType.getCompiledMainClasses(folder);
        return findPrivateMethods(classFiles,outputFile);
    }

    static Set<String> findPrivateMethods(Collection<File> classFiles, File outputFile) throws IOException {
        Preconditions.checkArgument(!classFiles.isEmpty(),"No .class files found, make sure that the project has been built");
        Set<String> elements = new HashSet<>();
        for (File classFile:classFiles) {
            // System.out.println("Analysing: " + classFile);
            try (InputStream in = new FileInputStream(classFile)) {
                new ClassReader(in).accept(new PrivateMethodFinder(elements), 0);
            }
        }
        if (outputFile!=null) {
            LOGGER.info("writing private methods to " + outputFile.getAbsolutePath());
            dumpPrivateMethods(elements,outputFile);
        }
        LOGGER.info(""+elements.size()+" elements identified");
        return elements;
    }

    static class PrivateMethodFinder extends ClassVisitor {
        Set<String> privateMethods = null;
        String currentClass = null;
        public PrivateMethodFinder(Set<String> privateMethods) {
            super(Opcodes.ASM9);
            this.privateMethods = privateMethods;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            this.currentClass = name.replace('/','.');
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            if (isPrivate(access)) {
                privateMethods.add(currentClass + "::" + name + descriptor);
            }
            return null;
        }

        private boolean isPrivate(int access) {
            if ((access & Opcodes.ACC_PUBLIC) != 0) return false;
            else if ((access & Opcodes.ACC_PROTECTED) != 0) return false;
            else return true;
        }

    }
}
