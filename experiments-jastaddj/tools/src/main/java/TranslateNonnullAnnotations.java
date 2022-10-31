import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Translate @NonNull annotations for all sources found in a folder.
 * Vintage implementation using Java 1.7 features.
 * @author jens dietrich
 */
public class TranslateNonnullAnnotations {

    public static final String OLD_ANNOTATION = "@NonNull";
    public static final String NEW_ANNOTATION = "@javax.annotation.Nonnull";

    public static void main (String[] args) throws Exception {
        if (args.length<1) throw new IllegalArgumentException("one arg required -- input folder containing Java source files");
        File src = new File(args[0]);
        if (!src.exists()) throw new IllegalArgumentException("folder does not exit: " + src.getAbsolutePath());
        Set<File> javaSources = CollectJavaFiles.collectJavaFiles(src);
        System.out.println("processing " + javaSources.size() + " java sources");


        for (File f:javaSources) {
            System.out.println("translating " + OLD_ANNOTATION + " -> " + NEW_ANNOTATION + " in " + f.getAbsolutePath());
            List<String> lines = readLines(f);
            try (PrintWriter w = new PrintWriter(new FileWriter(f))) {
                for (int i=0;i<lines.size();i++) {
                    lines.set(i,lines.get(i).replace(OLD_ANNOTATION,NEW_ANNOTATION));
                    w.println(lines.get(i));
                }
            }
            catch (Exception x) {
                System.err.println("Error rewriting annotations in " + f.getAbsolutePath());
                x.printStackTrace();
            }
        }

    }

    private static List<String> readLines(File f) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String line = null;
            while ((line = r.readLine())!=null) {
                lines.add(line);
            }
        } catch (Exception e) {
            System.err.println("Cannot read " + f.getAbsolutePath());
            e.printStackTrace();
        }
        return lines;
    }


}
