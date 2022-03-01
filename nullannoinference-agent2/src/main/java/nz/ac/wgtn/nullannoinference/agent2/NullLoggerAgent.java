package nz.ac.wgtn.nullannoinference.agent2;

import janala.instrument.SafeClassWriter;
import nz.ac.wgtn.nullannoinference.commons.IssueStore;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * ASM-based agent to log field writes.
 * @author jens dietrich
 */
public class NullLoggerAgent {

    public static final String PACKAGE_PREFIX = "nz.ac.wgtn.nullannoinference.includes";

    static void log(String msg) {
        System.out.println(NullLoggerAgent.class.getSimpleName() + ": " + msg);
    }

    private static String[] EXCLUDES = new String[]{"java/","javax/","org/objectweb/asm","sun/","com/sun/","jdk/"};
    // agentmain needed for testing
    public static void agentmain(String agentArgs, Instrumentation inst) {

        premain(agentArgs,inst);
    }
    public static void premain(String agentArgs, Instrumentation inst) {

        String prefix = System.getProperty(PACKAGE_PREFIX);
        if (prefix==null) {
            log("No class name prefix set, instrumenting all classes possible");
        }
        else {
            log("class name prefix set, instrumenting only classes with names starting with any of: " + prefix);
            prefix = prefix.replace('.','/');
        }
        final String prefix2 = prefix;
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader classLoader, String s, Class<?> aClass, ProtectionDomain protectionDomain, byte[] bytes) throws IllegalClassFormatException {


                for (String exclude:EXCLUDES) {
                    if (s.startsWith(exclude)) {
                        return bytes;
                    }
                }

                if (prefix2==null || s.startsWith(prefix2)) {

                    ClassReader reader = new ClassReader(bytes);

                    // this prevents some issues with the standard class writer leading to duplicate class errors
                    ClassWriter writer = new SafeClassWriter(reader,classLoader,ClassWriter.COMPUTE_FRAMES);
                    try {
                        CheckForNullableFieldAccess visitor = new CheckForNullableFieldAccess(writer);
                        reader.accept(visitor, 0);
                        return writer.toByteArray();
                    }
                    catch (Throwable x) {
                        x.printStackTrace();
                    }
                }
                return bytes;
            }
        });
    }


}
