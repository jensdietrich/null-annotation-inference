package nz.ac.wgtn.nullannoinference.annotator;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utilities
 * @author jens dietrich
 */
public class Utils {
    public static List<File> getFiles(String pattern) {

        boolean hasSep = pattern.contains(File.separator);
        String folderName = hasSep?pattern.substring(0,pattern.lastIndexOf(File.separator)):"";
        String namePattern = hasSep?pattern.substring(pattern.lastIndexOf(File.separator)+1):pattern;
        File folder = null;
        if (folderName.trim().isEmpty()) {
            folder = new File(System.getProperty("user.dir"));
        }
        else {
            folder = new File(folderName);
        }

        File[] files = folder.listFiles(
            file -> FilenameUtils.wildcardMatch(file.getName(),namePattern)
        );
        return Stream.of(files).collect(Collectors.toList());

    }
}
