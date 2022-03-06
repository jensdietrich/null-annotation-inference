

package nz.ac.wgtn.nullannoinference.annotator;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
 * Simple annotation listener producing a simple summary in json format.
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
    public static final String TOTAL_ISSUES = "total-issues";
    public static final String OPEN_ISSUES = "open-issues";

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
        String reportName = projectName + ".json";
        File report = new File(ANNOTATION_REPORT_FOLDER,reportName);

        LOGGER.info("exporting annotation summary to " + report.getAbsolutePath());

        int annotationsFailedCount = 0;
        for (File f:annotationFailed.keySet()) {
            annotationsFailedCount = annotationsFailedCount + annotationFailed.get(f).size();
        }

        // summarise as mao
        Map<String,Integer> counts = new HashMap<>();
        counts.put(ANNOTATED_JAVA_FILES,this.annotatedJavaFiles.size());
        counts.put(ANNOTATED_CLASSES,this.annotatedClasses.size());
        counts.put(ANNOTATED_METHODS,this.annotatedMethods.size());
        counts.put(ANNOTATED_ARGUMENTS,this.annotatedArgs.size());
        counts.put(ANNOTATED_RETURNS,this.annotatedReturns.size());
        counts.put(ANNOTATED_FIELDS,this.annotatedFields.size());
        counts.put(ANNOTATION_ERRORS,annotationsFailedCount);
        counts.put(OTHER_MODIFIED_FILE,this.otherwiseTransformedFiles.size());
        counts.put(TOTAL_ISSUES,this.issuesLoaded.size());
        counts.put(OPEN_ISSUES,this.openIssues.size());

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter out = new FileWriter(report)) {
            gson.toJson(counts,out);
        } catch (IOException e) {
            e.printStackTrace();
        }


        // export open issues for inspection and manual refactoring
        projectName = transformedProject.getName();
        String openIssuesFileName = projectName + "-open-issues.json";
        File openIssuesFile = new File(ANNOTATION_REPORT_FOLDER,openIssuesFileName);

        try (FileWriter out = new FileWriter(openIssuesFile)) {
            gson.toJson(this.openIssues,out);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void fileCopied(File originalFile, File copy) {
        // ignore
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
