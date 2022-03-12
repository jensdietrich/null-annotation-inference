package nz.ac.wgtn.nullannoinference.commons;

import java.io.File;
import java.util.List;

/**
 * Gradle project structure.
 * @author jens dietrich
 */
public class GradleProject implements Project {

    static final String TYPE = "gradle";

    @Override
    public List<File> getCompiledMainClasses(File projectRoot) {
        File folder = new File(projectRoot,"build/classes/java/main");
        if (!folder.exists() || folder.listFiles().length==0) {
            throw new IllegalStateException("Folder containing classes not found -- project must be build first: " + folder.getAbsolutePath());
        };
        return collectClassFiles(folder);
    }

    @Override
    public List<File> getCompiledTestClasses(File projectRoot) {
        File folder = new File(projectRoot,"build/classes/java/test");
        if (!folder.exists() || folder.listFiles().length==0) {
            throw new IllegalStateException("Folder containing classes not found -- project must be build first: " + folder.getAbsolutePath());
        };
        return collectClassFiles(folder);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void checkProjectRootFolder(File root) throws IllegalArgumentException {
        checkFolder(root);
        int gradlePresent = root.listFiles(f -> f.getName().endsWith(".gradle")).length;
        if (gradlePresent==0) {
            throw new IllegalArgumentException("No .gradle file found -- this is not a valid gradle project");
        }
    }
}
