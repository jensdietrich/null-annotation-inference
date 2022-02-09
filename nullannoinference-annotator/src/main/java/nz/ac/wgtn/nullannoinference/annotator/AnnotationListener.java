package nz.ac.wgtn.nullannoinference.annotator;

import java.io.File;

/**
 * Interface to log annotation results.
 * @author jens dietrich
 */
public interface AnnotationListener {
    void beforeAnnotationTransformation(File originalProject,File transformedProject);
    void afterAnnotationTransformation(File originalProject,File transformedProject);
    void fileCopied(File originalFile, File copy);
    void annotationsAdded(File originalFile, File transformedFile, int annotationsAddedCount);
    void annotationFailed(File originalFile, String reason);
    void configFileTransformed(File toFile, File copy);
}
