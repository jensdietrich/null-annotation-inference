package nz.ac.wgtn.nullannoinference.annotator;

import nz.ac.wgtn.nullannoinference.commons.Issue;

import java.io.File;

/**
 * Interface to log annotation results.
 * @author jens dietrich
 */
public interface AnnotationListener {
    void beforeAnnotationTransformation(File originalProject,File transformedProject);
    void afterAnnotationTransformation(File originalProject,File transformedProject);
    void fileCopied(File originalFile, File copy);
    void annotationAdded(File originalFile, File transformedFile,Issue issue);
    void annotationFailed(File originalFile, String reason);
    void configFileTransformed(File toFile, File copy);}
