package nz.ac.wgtn.nullannoinference.commons.projecttypes;

import nz.ac.wgtn.nullannoinference.commons.ProjectType;

import java.io.File;
import java.util.List;

/**
 * Special settings for guava where tests are in a separate module.
 * @author jens dietrich
 */
public class GuavaProject implements ProjectType  {
    public static final String TYPE = "special.guava";

    @Override
    public List<File> getCompiledMainClasses(File projectRoot) {
        File folder = new File(projectRoot,"guava/target/classes");
        if (!folder.exists() || folder.listFiles().length==0) {
            throw new IllegalStateException("Folder containing classes not found -- project must be build first with \"mvn compile\": " + folder.getAbsolutePath());
        };
        return collectClassFiles(folder);
    }

    @Override
    public List<File> getCompiledTestClasses(File projectRoot) {
        File folder = new File(projectRoot,"guava-tests/target/test-classes");
        if (!folder.exists() || folder.listFiles().length==0) {
            throw new IllegalStateException("Folder containing classes not found -- project must be build first with \"mvn test-compile\": " + folder.getAbsolutePath());
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
        if (!new File(root,"pom.xml").exists()) {
            throw new IllegalArgumentException("No pom.xml file found -- this is not a valid mvn project");
        }
    }

}
