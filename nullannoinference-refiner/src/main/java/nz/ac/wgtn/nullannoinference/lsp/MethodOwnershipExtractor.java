package nz.ac.wgtn.nullannoinference.lsp;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Objects;

public class MethodOwnershipExtractor {

    public static Multimap<Method,String> extractMethodOwnership (File... classLocations) throws IOException {
        Multimap<Method,String> ownershipMap = HashMultimap.create();
        for (File projectFolder:classLocations) {
            add(ownershipMap,projectFolder);
        }
        return ownershipMap;
    }

    private static void add(Multimap<Method,String> ownershipMap,File project) throws IOException {
        File compiledTestClasses = new File(project,"target/classes");
        if (!compiledTestClasses.exists()) {
            throw new IllegalStateException("project must be built before analysis can be found (mvn class)");
        }
        Collection<File> classFiles = FileUtils.listFiles(compiledTestClasses,new String[]{"class"},true);
        if (classFiles.isEmpty()) {
            throw new IllegalStateException("No .class files found, make sure that the project has been built");
        }
        for (File classFile:FileUtils.listFiles(project,new String[]{"class"},true)) {
            // System.out.println("Analysing: " + classFile);
            try (InputStream in = new FileInputStream(classFile)) {
                new ClassReader(in).accept(new MethodOwnershipVistor(ownershipMap), 0);
            }
        }
    }

    static class MethodOwnershipVistor extends ClassVisitor {
        private Multimap<Method,String> ownershipMap = null;
        private String className = null;

        public MethodOwnershipVistor(Multimap<Method,String> ownershipMap) {
            super(Opcodes.ASM9);
            this.ownershipMap = ownershipMap;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            this.className = name.replace('/','.');
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            super.visitMethod(access, name, descriptor, signature, exceptions);
            // ignore constructors and static blocks !
            if (!Objects.equals(name,"<init>") && !Objects.equals(name,"<clinit>")) {
                Method method = new Method(name, descriptor);
                ownershipMap.put(method, className);
            }
            return null;
        }
    }
}
