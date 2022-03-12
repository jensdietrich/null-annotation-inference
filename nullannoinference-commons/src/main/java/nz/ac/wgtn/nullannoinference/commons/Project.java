package nz.ac.wgtn.nullannoinference.commons;

import java.io.File;

/**
 * Abstraction for (the relevant parts) of a project to be analysed.
 * This does currently not support projects with several modules.
 * @author jens dietrich
 */
public interface Project {
    default void checkFolder(File folder) {
        if (folder==null) {
            throw new IllegalArgumentException("folder must not be null");
        }
        if (!folder.exists()) {
            throw new IllegalArgumentException("folder does not exist: " + folder.getAbsolutePath());
        }
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("this is not a folder: " + folder.getAbsolutePath());
        }
    }
    File getCompiledMainClassesFolder(File projectRoot);
    File getCompiledTestClassesFolder(File projectRoot);

}
