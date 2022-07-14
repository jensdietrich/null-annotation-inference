package nz.ac.wgtn.nullannoinference.commons;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Abstraction for (the relevant parts) of a project to be analysed.
 * This does currently not support projects with several modules.
 * @author jens dietrich
 */
public interface Project {


    Project MVN = new MavenProject();
    Project GRADLE = new GradleProject();
    Project MULTI_GRADLE = new MultiLanguageGradleProject();

    static String[] getValidProjectTypes() {
        return new String[]{MavenProject.TYPE,GradleProject.TYPE,MultiLanguageGradleProject.TYPE};
    }
    static Project getProject(String name) {
        if (name==null || name.equals(MavenProject.TYPE)) {
            return MVN;
        }
        else if (name.equals(GradleProject.TYPE)) {
            return GRADLE;
        }
        else if (name.equals(MultiLanguageGradleProject.TYPE)) {
            return MULTI_GRADLE;
        }
        else {
            throw new IllegalArgumentException("Not known project type: " + name);
        }
    }

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

    default List<File> collectFiles(Predicate<File> filter,File... folders) {
        List<File> fileList = new ArrayList<>();
        for (File folder:folders) {
            if (folder.exists()) {
                try {
                    Files.walkFileTree(folder.toPath(), new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path p, BasicFileAttributes attrs) throws IOException {
                            File file = p.toFile();
                            if (filter.test(file)) {
                                fileList.add(file);
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } catch (IOException x) {
                    x.printStackTrace();
                }
            }
        }
        return fileList;
    }

    default List<File> collectClassFiles(File... folders) {
        Predicate<File> isClassFile = f -> f.exists() && !f.isDirectory() && f.getName().endsWith(".class");
        return collectFiles(isClassFile,folders);
    }


    List<File> getCompiledMainClasses(File projectRoot);
    List<File> getCompiledTestClasses(File projectRoot);

    String getType();

    void checkProjectRootFolder(File root) throws IllegalArgumentException;

}
