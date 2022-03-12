package nz.ac.wgtn.nullannoinference.commons;

import java.io.File;

/**
 * Maven project structure.
 * @author jens dietrich
 */
public class MavenProject implements Project {

    @Override
    public File getCompiledMainClassesFolder(File projectRoot) {
        checkFolder(projectRoot);
        File folder = new File(projectRoot,"target/classes");
        if (!folder.exists() || folder.listFiles().length==0) {
            throw new IllegalStateException("Folder containing classes not found -- project must be build first with \"mvn compile\": " + folder.getAbsolutePath());
        };
        return folder;
    }

    @Override
    public File getCompiledTestClassesFolder(File projectRoot) {
        checkFolder(projectRoot);
        File folder = new File(projectRoot,"target/test-classes");
        if (!folder.exists() || folder.listFiles().length==0) {
            throw new IllegalStateException("Folder containing classes not found -- project must be build first with \"mvn test-compile\": " + folder.getAbsolutePath());
        };
        return folder;
    }

    @Override
    public String getType() {
        return "maven";
    }

    @Override
    public void checkProjectRootFolder(File root) throws IllegalArgumentException {
        checkFolder(root);
        if (!new File(root,"pom.xml").exists()) {
            throw new IllegalArgumentException("No pom.xml file found -- this is not a valid mvn project");
        }
    }
}
