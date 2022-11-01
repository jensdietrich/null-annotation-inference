import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

/**
 * Collect Java sources, replace src root with variable ref, write to file to be added to list of
 * Java sources to be annotated in a sh script.
 * Vintage implementation using Java 1.7 features.
 * @author jens dietrich
 */
public class CollectJavaFiles {

    public static final String REPLACE_ROOT = "$SRC";

    public static void main (String[] args) throws Exception {
        if (args.length<2) throw new IllegalArgumentException("two args required -- input folder and file to write list of Java files to");
        File src = new File(args[0]);
        if (!src.exists()) throw new IllegalArgumentException("folder does not exit: " + src.getAbsolutePath());
        Set<File> javaSources = CollectJavaFiles.collectJavaFiles(src);
        System.out.println("processing " + javaSources.size() + " java sources");

        File out = new File(args[1]);
        String srcPath = src.getAbsolutePath();
        Set<File> srcs = collectJavaFiles(src);
        StringBuilder b = new StringBuilder();

        for (File f:srcs) {
            String name = f.getAbsolutePath();
            name = name.replace(srcPath,REPLACE_ROOT);
            if (b.length()>0) b.append(" ");
            b.append(name);
        }

        try (PrintWriter w = new PrintWriter(out)) {
            w.println(b.toString());
        }

        System.out.println("src list written to " + out.getAbsolutePath());
    }


    static Set<File> collectJavaFiles(File folder) throws IOException {
        Set<File> files = new HashSet<>();
        collectJavaFiles(folder,files);
        return files;
    }

    private static void collectJavaFiles(File folder,Set<File> collector) {
        for (File f:folder.listFiles()) {
            if (f.isDirectory()) {
                collectJavaFiles(f,collector);
            }
            else if (f.getName().endsWith(".java") && !f.getName().equals("package-info.java")) {
                collector.add(f);
            }
        }
    }
}
