

package nz.ac.wgtn.nullannoinference.annotator;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Simple annotation listener producing a simple summary text file.
 * @author jens dietrich
 */
public class LoggingAnnotationListener implements AnnotationListener {

    // keys
    public static final String ANNOTATED_JAVA_FILES = "annotated-java-files";
    public static final String ANNOTATED_CLASSES = "annotated-classes";
    public static final String ANNOTATED_METHODS = "annotated-methods";
    public static final String ANNOTATED_FIELDS = "annotated-fields";
    public static final String ANNOTATED_ARGUMENTS = "annotated-args";
    public static final String ANNOTATED_RETURNS = "annotated-returns";
    public static final String ANNOTATION_ERRORS = "annotated-error";
    public static final String OTHER_MODIFIED_FILE = "other-files-modified";

    public static final Logger LOGGER = LogSystem.getLogger("annotator-result-export");


    private Set<File> annotatedJavaFiles = new HashSet<>();
    private Set<String> annotatedClasses = new HashSet<>();
    private Set<String> annotatedMethods = new HashSet<>();
    private Set<String> annotatedFields = new HashSet<>();
    private Set<String> annotatedReturns = new HashSet<>();
    private Set<String> annotatedArgs = new HashSet<>();
    private Multimap<File,String> annotationFailed = HashMultimap.create();
    private List<File> otherwiseTransformedFiles = new ArrayList<>();
    private Set<Issue> issuesLoaded = null;
    private Set<Issue> openIssues = null;

    private File ANNOTATION_REPORT_FOLDER = new File(".annotation-results");

    @Override
    public void annotationAdded(File originalFile, File transformedFile, Issue issue) {
        this.annotatedJavaFiles.add(originalFile);
        this.annotatedClasses.add(issue.getClassName());
        boolean added = false;
        if (issue.getKind()==Issue.IssueType.FIELD) {
            added = this.annotatedFields.add(issue.getClassName()+"::"+issue.getMethodName());
        }
        else if (issue.getKind()==Issue.IssueType.ARGUMENT) {
            this.annotatedMethods.add(issue.getClassName()+"::"+issue.getMethodName());
            added = this.annotatedArgs.add(issue.getClassName()+"::"+issue.getMethodName()+issue.getDescriptor()+"@"+issue.getArgsIndex());
        }
        else if (issue.getKind()==Issue.IssueType.RETURN_VALUE) {
            this.annotatedMethods.add(issue.getClassName()+"::"+issue.getMethodName());
            added = this.annotatedReturns.add(issue.getClassName()+"::"+issue.getMethodName()+issue.getDescriptor());
        }
        else {
            LOGGER.warn("unknown issue type encountered: " + issue.getKind());
        }
        assert added;
        assert openIssues!=null;
        if (added) {
            openIssues.remove(issue);
        }
    }

    @Override
    public void beforeAnnotationTransformation(File originalProject, File transformedProject) {

        if (!ANNOTATION_REPORT_FOLDER.exists()) {
            ANNOTATION_REPORT_FOLDER.mkdirs();
        }
    }

    @Override
    public void afterAnnotationTransformation(File originalProject, File transformedProject) {

        // write report
        String projectName = transformedProject.getName();
        String reportName = "annotation-summary-" + projectName + ".csv";
        File report = new File(ANNOTATION_REPORT_FOLDER,reportName);

        LOGGER.info("exporting annotation summary to " + report.getAbsolutePath());

        int annotationsFailedCount = 0;
        for (File f:annotationFailed.keySet()) {
            annotationsFailedCount = annotationsFailedCount + annotationFailed.get(f).size();
        }

        try (PrintWriter out = new PrintWriter(new FileWriter(report))) {
            String[] keys = new String[]{ANNOTATED_JAVA_FILES,ANNOTATED_CLASSES,ANNOTATED_METHODS,ANNOTATED_ARGUMENTS,ANNOTATED_RETURNS,ANNOTATED_FIELDS,ANNOTATION_ERRORS,OTHER_MODIFIED_FILE};
            out.println(Stream.of(keys).collect(Collectors.joining("\t")));
            out.print(this.annotatedJavaFiles.size());
            out.print("\t");
            out.print(this.annotatedClasses.size());
            out.print("\t");
            out.print(this.annotatedMethods.size());
            out.print("\t");
            out.print(this.annotatedArgs.size());
            out.print("\t");
            out.print(this.annotatedReturns.size());
            out.print("\t");
            out.print(this.annotatedFields.size());
            out.print("\t");
            out.print(this.otherwiseTransformedFiles.size());
            out.print("\t");
            out.print(annotationsFailedCount);
            out.println();
            LOGGER.info("Annotation results written to " + report.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.warn("error writing annotation summary",e);
        }
    }

    @Override
    public void fileCopied(File originalFile, File copy) {
        // dont log here
    }

    @Override
    public void configFileTransformed(File toFile, File copy) {
        otherwiseTransformedFiles.add(copy);
    }

    @Override
    public void annotationFailed(File originalFile, String reason) {
        annotationFailed.put(originalFile,reason);
    }

    @Override
    public void issuesLoaded(Set<Issue> issues) {
        this.issuesLoaded = new HashSet<>();
        this.issuesLoaded.addAll(issues);
        this.openIssues = new HashSet<>();
        this.openIssues.addAll(issues);
    }
}
