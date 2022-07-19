package nz.ac.wgtn.nullannoinference.propagator;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
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

/**
 * Extract information about which classes declare methods.
 * @author jens dietrich
 */
public class MethodOwnershipExtractor {

    public static Multimap<MethodInfo,String> extractMethodOwnership (Collection<File> classFiles) throws IOException {
        Multimap<MethodInfo,String> ownershipMap = HashMultimap.create();
        add(ownershipMap,classFiles);
        return ownershipMap;
    }

    private static void add(Multimap<MethodInfo,String> ownershipMap, Collection<File> classFiles) throws IOException {
        for (File classFile:classFiles) {
            // System.out.println("Analysing: " + classFile);
            try (InputStream in = new FileInputStream(classFile)) {
                new ClassReader(in).accept(new MethodOwnershipVistor(ownershipMap), 0);
            }
        }
    }

    static class MethodOwnershipVistor extends ClassVisitor {
        private Multimap<MethodInfo,String> ownershipMap = null;
        private String className = null;

        public MethodOwnershipVistor(Multimap<MethodInfo,String> ownershipMap) {
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
                MethodInfo method = new MethodInfo(name, descriptor);
                ownershipMap.put(method, className);
            }
            return null;
        }
    }
}
