package nz.ac.wgtn.nullannoinference.commons;

import nz.ac.wgtn.nullannoinference.commons.projecttypes.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Abstraction for (the relevant parts) of a project to be analysed.
 * This does currently not support projects with several modules.
 * @author jens dietrich
 */
public interface ProjectType {

    ProjectType MVN = new MavenProject();
    ProjectType GRADLE = new GradleProject();
    ProjectType MULTI_GRADLE = new MultiLanguageGradleProject();
    ProjectType GUAVA = new GuavaProject();
    ProjectType ERROR_PRONE = new ErrorProneProject();

    // implementation languages
    String LANG_JAVA = "java";
    String LANG_KOTLIN= "kotlin";
    String LANG_GROOVY = "groovy";

    static String[] getValidProjectTypes() {
        return new String[]{MavenProject.TYPE,GradleProject.TYPE,MultiLanguageGradleProject.TYPE, GuavaProject.TYPE, ErrorProneProject.TYPE};
    }

    static String getValidProjectTypesAsString() {
        return Stream.of(getValidProjectTypes()).collect(Collectors.joining(","));
    }

    static ProjectType getProject(String name) {
        if (name==null || name.equals(MavenProject.TYPE)) {
            return MVN;
        }
        else if (name.equals(GradleProject.TYPE)) {
            return GRADLE;
        }
        else if (name.equals(MultiLanguageGradleProject.TYPE)) {
            return MULTI_GRADLE;
        }
        else if (name.equals(GuavaProject.TYPE)) {
            return GUAVA;
        }
        else if (name.equals(ERROR_PRONE.getType())) {
            return ERROR_PRONE;
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

    default String getImplementationLanguage(File classFile) {
        assert classFile.getAbsolutePath().endsWith(".class");
        return LANG_JAVA;
    }

}
