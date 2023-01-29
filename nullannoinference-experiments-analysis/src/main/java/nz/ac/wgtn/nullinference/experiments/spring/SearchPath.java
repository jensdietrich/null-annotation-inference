package nz.ac.wgtn.nullinference.experiments.spring;

import java.io.File;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Abstraction for a search path consisting opf multiple folders.
 * @authoe jens dietrich
 */
public class SearchPath {

    private File[] folders = null;

    public SearchPath(File... folders) {
        this.folders = folders;
    }

    public static SearchPath of(File... folders) {
        return new SearchPath(folders);
    }

    File getData(String name) {
        String fileName = "nullable-" + name + ".json";
        for (File folder:folders) {
            File file = new File(folder,fileName);
            if (file.exists()) {
                System.out.println("data file located: " + file.getAbsolutePath());
                return file;
            }
        }
        throw new IllegalStateException("File named " + name + " not found in path: " + Stream.of(folders).map(f -> f.getAbsolutePath()).collect(Collectors.joining(",")));
    }


}
