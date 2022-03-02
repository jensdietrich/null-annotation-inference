

package nz.ac.wgtn.nullannoinference.annotator;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import nz.ac.wgtn.nullannoinference.commons.Issue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Simple annotation listener producing a simple summary text file.
 * @author jens dietrich
 */
public class LoggingAnnotationListener implements AnnotationListener {

    private List<File> copiedFiles = new ArrayList<>();
    private Set<File> annotatedJavaFiles = new HashSet<>();
    private Multimap<File,String> annotationFailed = HashMultimap.create();
    private List<File> otherwiseTransformedFiles = new ArrayList<>();
    private File ANNOTATION_REPORT_FOLDER = new File(".annotation-results");

    @Override
    public void annotationAdded(File originalFile, File transformedFile, String className, String methodOtFieldName, String descriptor, int index, Issue.IssueType kind) {

    }

    @Override
    public void beforeAnnotationTransformation(File originalProject, File transformedProject) {
        this.copiedFiles.clear();
        this.annotatedJavaFiles.clear();
        this.otherwiseTransformedFiles.clear();
        this.annotationFailed.clear();

        if (!ANNOTATION_REPORT_FOLDER.exists()) {
            ANNOTATION_REPORT_FOLDER.mkdirs();
        }
    }

    @Override
    public void afterAnnotationTransformation(File originalProject, File transformedProject) {

        //        TODO redo summary logging

//        // write report
//        String projectName = transformedProject.getName();
//        String reportName = "annotation-summary-" + projectName + ".txt";
//        File report = new File(ANNOTATION_REPORT_FOLDER,reportName);
//
//        int totalAnnotationsInsertedCount = 0;
//        for (int count:annotatedJavaFiles.values()) {
//            totalAnnotationsInsertedCount = totalAnnotationsInsertedCount + count;
//        }
//        int annotationsFailedCount = 0;
//        for (File f:annotationFailed.keySet()) {
//            annotationsFailedCount = annotationsFailedCount + annotationFailed.get(f).size();
//        }
//
//        try (PrintWriter out = new PrintWriter(new FileWriter(report))) {
//            out.println("annotated Java files: " + annotatedJavaFiles.size());
//            out.println("annotations inserted: " + totalAnnotationsInsertedCount);
//            out.println("annotations failed (file count): " + annotationFailed.keySet().size());
//            out.println("annotations failed (issue count): " + annotationsFailedCount);
//            out.println("other transformed files: " + otherwiseTransformedFiles.size());
//            out.println("copied files: " + copiedFiles.size());
//            out.println();
//
//            out.println("Details of annotated files ( \"<file> -> <number of annotations injected>\") ");
//            for (File annotatedFile:annotatedJavaFiles.keySet()) {
//                out.println("\t"+annotatedFile.getAbsolutePath() + " -> " + annotatedJavaFiles.get(annotatedFile));
//            }
//            out.println();
//
//            out.println("Details of failed annotations ( \"<file> -> <reason>\") ");
//            for (File f:annotationFailed.keySet()) {
//                for (String reason:annotationFailed.get(f)) {
//                    out.println("\t"+f.getAbsolutePath() + " -> " + reason);
//                }
//            }
//            out.println();
//
//            out.println("Details of otherwise transformed files");
//            for (File transformedFile:otherwiseTransformedFiles) {
//                out.println("\t"+transformedFile.getAbsolutePath());
//            }
//
//            System.out.println("Annotation results written to " + report.getAbsolutePath());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void fileCopied(File originalFile, File copy) {
        copiedFiles.add(copy);
    }


    @Override
    public void configFileTransformed(File toFile, File copy) {
        Preconditions.checkState(!copiedFiles.contains(copy));
        otherwiseTransformedFiles.add(copy);
    }

    @Override
    public void annotationFailed(File originalFile, String reason) {
        annotationFailed.put(originalFile,reason);
    }
}
