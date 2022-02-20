package nz.ac.wgtn.nullannoinference.agent2;

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

    static {
        Thread saveResult = new Thread(() -> IssueStore.save());
        Runtime.getRuntime().addShutdownHook(saveResult);
    }

    public static final String PACKAGE_PREFIX = "nz.ac.wgtn.nullannoinference.includes";

    static void log(String msg) {
        System.out.println(NullLoggerAgent.class.getSimpleName() + ": " + msg);
    }


    // ahentmain needed for testing
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

                if (prefix2==null || s.startsWith(prefix2)) {
                    ClassReader reader = new ClassReader(bytes);
                    ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
                    try {
                        CheckForNullableFieldAccess visitor = new CheckForNullableFieldAccess(writer);
                        reader.accept(visitor, 0);
                        return writer.toByteArray();
                    }
                    catch (Throwable x) {
                        x.printStackTrace();
                    }
                }
                return null;
            }
        });
    }


}
