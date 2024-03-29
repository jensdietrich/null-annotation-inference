package nz.ac.wgtn.nullannoinference.commons.projecttypes;

import nz.ac.wgtn.nullannoinference.commons.ProjectType;

import java.io.File;
import java.util.List;

/**
 * Gradle project structure. Also looks for compiled classes from groovy and kotlin sources.
 * Note that some spring projects (spring-beans 5.3.9) might place compiled java classes in build/classes/groovy
 * @author jens dietrich
 */
public class MultiLanguageGradleProject implements ProjectType {

    public static final String TYPE = "gradle_multilang";

    @Override
    public List<File> getCompiledMainClasses(File projectRoot) {
        File javaClasses = new File(projectRoot,"build/classes/java/main");
        File groovyClasses = new File(projectRoot,"build/classes/groovy/main");
        File kotlinClasses = new File(projectRoot,"build/classes/kotlin/main");
        return collectClassFiles(javaClasses,groovyClasses,kotlinClasses);
    }

    @Override
    public List<File> getCompiledTestClasses(File projectRoot) {
        File javaClasses = new File(projectRoot,"build/classes/java/test");
        File groovyClasses = new File(projectRoot,"build/classes/groovy/test");
        File kotlinClasses = new File(projectRoot,"build/classes/kotlin/test");
        return collectClassFiles(javaClasses,groovyClasses,kotlinClasses);
    }

    @Override
    public String getImplementationLanguage(File classFile) {
        if (classFile.getAbsolutePath().contains("build/classes/groovy/")) {
            return ProjectType.LANG_GROOVY;
        }
        else if (classFile.getAbsolutePath().contains("build/classes/kotlin/")) {
            return ProjectType.LANG_KOTLIN;
        }
        else {
            return ProjectType.super.getImplementationLanguage(classFile);
        }
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
