package nz.ac.wgtn.nullinference.experiments.spring;

import com.google.common.base.Preconditions;
import nz.ac.wgtn.nullannoinference.commons.AbstractIssue;
import nz.ac.wgtn.nullannoinference.commons.Issue;

import java.io.File;
import java.util.List;
import java.util.function.Predicate;

/**
 * Experiment configurations.
 * @author jens dietrich
 */

public class Config {
     public static final List<String> SPRING_MODULES = List.of(
        "spring-beans",
        "spring-context",
        "spring-core",
        "spring-orm",
        "spring-oxm",
        "spring-web",
        "spring-webmvc"
    );

    public static final File EXTRACTED_ISSUES_FOLDER = new File("experiments-spring/results/extracted");
    public static final File EXTRACTED_PLUS_ISSUES_FOLDER = new File("experiments-spring/results/extracted+");
    public static final File OBSERVED_ISSUES_FOLDER = new File("experiments-spring/results/observed");
    public static final File OBSERVED_AND_PROPAGATED_ISSUES_FOLDER = new File("experiments-spring/results/observed+");

    public static final File SANITIZED_ISSUES_FOLDER = new File("experiments-spring/results/sanitized");
    public static final File SANITIZED_ISSUES_DEPRECATED_FOLDER = new File("experiments-spring/results/sanitizedD");
    public static final File SANITIZED_ISSUES_MAINSCOPE_FOLDER = new File("experiments-spring/results/sanitizedM");

    public static final File SANITIZED_ISSUES_NONPRIVATEMETHODS_FOLDER = new File("experiments-spring/results/sanitizedP");
    public static final File SANITIZED_ISSUES_NEGATIVETESTS_FOLDER = new File("experiments-spring/results/sanitizedN");
    public static final File SANITIZED_ISSUES_SHADED_FOLDER = new File("experiments-spring/results/sanitizedS");

    public static final String SANITIZER_NAMES = "D - deprecation, M - main scope, N - negative tests, S - shading";

    public static final Predicate<Issue> FIELDS_ONLY = issue -> issue.getKind()== Issue.IssueType.FIELD;
    public static final Predicate<Issue> PARAM_ONLY = issue -> issue.getKind()== Issue.IssueType.ARGUMENT;
    public static final Predicate<Issue> RETURNS_ONLY = issue -> issue.getKind()== Issue.IssueType.RETURN_VALUE;
    public static final Predicate<Issue> ALL = issue -> true;


    static {
        Preconditions.checkArgument(EXTRACTED_ISSUES_FOLDER.exists());
        Preconditions.checkArgument(EXTRACTED_ISSUES_FOLDER.isDirectory());
        Preconditions.checkArgument(EXTRACTED_PLUS_ISSUES_FOLDER.exists());
        Preconditions.checkArgument(EXTRACTED_PLUS_ISSUES_FOLDER.isDirectory());
        Preconditions.checkArgument(OBSERVED_ISSUES_FOLDER.exists());
        Preconditions.checkArgument(OBSERVED_ISSUES_FOLDER.isDirectory());
        Preconditions.checkArgument(OBSERVED_AND_PROPAGATED_ISSUES_FOLDER.exists());
        Preconditions.checkArgument(OBSERVED_AND_PROPAGATED_ISSUES_FOLDER.isDirectory());
        Preconditions.checkArgument(SANITIZED_ISSUES_FOLDER.exists());
        Preconditions.checkArgument(SANITIZED_ISSUES_FOLDER.isDirectory());
        Preconditions.checkArgument(SANITIZED_ISSUES_SHADED_FOLDER.exists());
        Preconditions.checkArgument(SANITIZED_ISSUES_SHADED_FOLDER.isDirectory());
        Preconditions.checkArgument(SANITIZED_ISSUES_DEPRECATED_FOLDER.exists());
        Preconditions.checkArgument(SANITIZED_ISSUES_DEPRECATED_FOLDER.isDirectory());
        Preconditions.checkArgument(SANITIZED_ISSUES_MAINSCOPE_FOLDER.exists());
        Preconditions.checkArgument(SANITIZED_ISSUES_MAINSCOPE_FOLDER.isDirectory());
        Preconditions.checkArgument(SANITIZED_ISSUES_NEGATIVETESTS_FOLDER.exists());
        Preconditions.checkArgument(SANITIZED_ISSUES_NEGATIVETESTS_FOLDER.isDirectory());
    }

}
