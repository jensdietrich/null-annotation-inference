import descr.DescriptorParser;
import descr.MethodDescriptor;
import org.objectweb.asm.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Script to analyse the number of fields, method returns and arguments that can be null annotated.
 * @author jens dietrich
 */
public class CountAnnotatables {

    enum KEY {field,method_returns,method_args}

    public static void main (String[] args) {
        if (args.length<1) {
            throw new IllegalArgumentException("One arguments expected -- a folder containing a built maven project,");
        }
        File projectFolder = new File(args[0]);

        Map<KEY,Integer> counters = findAnnotatables(projectFolder);
        System.out.println("Counted annotatables in: " + projectFolder.getAbsolutePath());
        System.out.println("Annotatable fields: " + counters.get(KEY.field));
        System.out.println("Annotatable method returns: " + counters.get(KEY.method_returns));
        System.out.println("Annotatable method arguments: " + counters.get(KEY.method_args));
        System.out.println("Annotatables total: " + (counters.get(KEY.field) + counters.get(KEY.method_returns) + counters.get(KEY.method_args)));

    }

    public static Map<KEY,Integer> findAnnotatables( File projectFolder) {

        File binClassFolder = new File(projectFolder,"target/classes");
        Set<File> classFiles = ExtractNonNullAnnotations.collectJavaFiles(binClassFolder);
        Map<KEY,Integer> counters = new HashMap<>();
        counters.put(KEY.field,0);
        counters.put(KEY.method_args,0);
        counters.put(KEY.method_returns,0);

        for (File classFile:classFiles) {
            try (InputStream in = new FileInputStream(classFile)) {
                new ClassReader(in).accept(new AnnotatableCollector(counters), 0);
            }
            catch (Exception x) {
                x.printStackTrace();
            }
        }

        return counters;
    }

    static class AnnotatableCollector extends ClassVisitor {
        Map<KEY,Integer> counters = null;

        public AnnotatableCollector(Map<KEY,Integer> counters) {
            super(Opcodes.ASM5);
            this.counters = counters;
        }

        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodDescriptor descr = DescriptorParser.parseMethodDescriptor(descriptor);
            if (DescriptorParser.isNullable(descr.getReturnType())) {
                counters.put(KEY.method_returns,counters.get(KEY.method_returns)+1);
            }
            for (String paramType:descr.getParamTypes()) {
                if (DescriptorParser.isNullable(paramType)) {
                    counters.put(KEY.method_args, counters.get(KEY.method_args) + 1);
                }
            }
            return null;
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            String descr = DescriptorParser.parseFieldDescriptor(descriptor);
            if (DescriptorParser.isNullable(descr)) {
                counters.put(KEY.field,counters.get(KEY.field)+1);
            }
            return null;
        }
    }

}

