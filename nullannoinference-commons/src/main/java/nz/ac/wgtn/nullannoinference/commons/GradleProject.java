package nz.ac.wgtn.nullannoinference.commons;

import java.io.File;

/**
 * Gradle project structure.
 * @author jens dietrich
 */
public class GradleProject implements Project {

    @Override
    public File getCompiledMainClassesFolder(File projectRoot) {
        checkFolder(projectRoot);
        File folder = new File(projectRoot,"build/classes/java/main");
        if (!folder.exists() || folder.listFiles().length==0) {
            throw new IllegalStateException("Folder containing classes not found -- project must be build first: " + folder.getAbsolutePath());
        };
        return folder;
    }

    @Override
    public File getCompiledTestClassesFolder(File projectRoot) {
        checkFolder(projectRoot);
        File folder = new File(projectRoot,"build/classes/java/test");
        if (!folder.exists() || folder.listFiles().length==0) {
            throw new IllegalStateException("Folder containing classes not found -- project must be build first: " + folder.getAbsolutePath());
        };
        return folder;
    }
}
