package nz.ac.wgtn.nullinference.experiments.spring;

import com.google.common.base.Preconditions;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.IssueKernel;

import java.io.File;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static final List<String> ADDITIONAL_PROGRAMS = List.of(
        "guava",
        "error-prone"
    );

    public static final List<String> FULL_DATESET = Stream.concat(
        SPRING_MODULES.stream(),ADDITIONAL_PROGRAMS.stream()
    ).collect(Collectors.toList());

    public static final File SPRING_RESULTS = new File("experiments-spring/results/");
    public static final File ADDITIONAL_RESULTS = new File("experiments-additional/results/");

    private static File[] getResultFolders(String name) {
        return new File[] {
            new File(SPRING_RESULTS,name),
            new File(ADDITIONAL_RESULTS,name)
        };
    };

    public static final SearchPath EXTRACTED_ISSUES_FOLDER = SearchPath.of(getResultFolders("extracted"));
    public static final SearchPath EXTRACTED_FROM_REANNOTATED_ISSUES_FOLDER = SearchPath.of(getResultFolders("extracted-reannotated"));
    public static final SearchPath EXTRACTED_PLUS_ISSUES_FOLDER = SearchPath.of(getResultFolders("extracted+"));
    public static final SearchPath OBSERVED_ISSUES_FOLDER = SearchPath.of(getResultFolders("observed"));
    public static final SearchPath OBSERVED_AND_PROPAGATED_ISSUES_FOLDER = SearchPath.of(getResultFolders("observed+"));

    public static final SearchPath OBSERVED_AND_PROPAGATED_SANITIZED_ISSUES_FOLDER = SearchPath.of(getResultFolders("observed++"));

    public static final SearchPath SANITIZED_ISSUES_FOLDER = SearchPath.of(getResultFolders("sanitized"));
    public static final SearchPath SANITIZED_ISSUES_DEPRECATED_FOLDER = SearchPath.of(getResultFolders("sanitizedD"));
    public static final SearchPath SANITIZED_ISSUES_MAINSCOPE_FOLDER = SearchPath.of(getResultFolders("sanitizedM"));

    public static final SearchPath SANITIZED_ISSUES_NONPRIVATEMETHODS_FOLDER = SearchPath.of(getResultFolders("sanitizedP"));
    public static final SearchPath SANITIZED_ISSUES_NEGATIVETESTS_FOLDER = SearchPath.of(getResultFolders("sanitizedN"));
    public static final SearchPath SANITIZED_ISSUES_SHADED_FOLDER = SearchPath.of(getResultFolders("sanitizedS"));

    public static final String SANITIZER_NAMES = "D - deprecation, M - main scope, N - negative tests, S - shading";

    public static final File PROJECTS = new File("experiments-spring/projects/original/spring-framework");

    public static final Predicate<Issue> FIELDS_ONLY = issue -> issue.getKind()== Issue.IssueType.FIELD;
    public static final Predicate<Issue> PARAM_ONLY = issue -> issue.getKind()== Issue.IssueType.ARGUMENT;
    public static final Predicate<Issue> RETURNS_ONLY = issue -> issue.getKind()== Issue.IssueType.RETURN_VALUE;

    public static final Predicate<IssueKernel> AGGR_FIELDS_ONLY = issue -> issue.getKind()== Issue.IssueType.FIELD;
    public static final Predicate<IssueKernel> AGGR_PARAM_ONLY = issue -> issue.getKind()== Issue.IssueType.ARGUMENT;
    public static final Predicate<IssueKernel> AGGR_RETURNS_ONLY = issue -> issue.getKind()== Issue.IssueType.RETURN_VALUE;

    public static final Predicate<IssueKernel> AGGRE_ALL = issue -> true;
    public static final Predicate<Issue> ALL = issue -> true;


//    static {
//        Preconditions.checkArgument(EXTRACTED_ISSUES_FOLDER.exists());
//        Preconditions.checkArgument(EXTRACTED_ISSUES_FOLDER.isDirectory());
//        Preconditions.checkArgument(EXTRACTED_PLUS_ISSUES_FOLDER.exists());
//        Preconditions.checkArgument(EXTRACTED_PLUS_ISSUES_FOLDER.isDirectory());
//        Preconditions.checkArgument(OBSERVED_ISSUES_FOLDER.exists());
//        Preconditions.checkArgument(OBSERVED_ISSUES_FOLDER.isDirectory());
//        Preconditions.checkArgument(OBSERVED_AND_PROPAGATED_ISSUES_FOLDER.exists());
//        Preconditions.checkArgument(OBSERVED_AND_PROPAGATED_ISSUES_FOLDER.isDirectory());
//        Preconditions.checkArgument(SANITIZED_ISSUES_FOLDER.exists());
//        Preconditions.checkArgument(SANITIZED_ISSUES_FOLDER.isDirectory());
//        Preconditions.checkArgument(SANITIZED_ISSUES_SHADED_FOLDER.exists());
//        Preconditions.checkArgument(SANITIZED_ISSUES_SHADED_FOLDER.isDirectory());
//        Preconditions.checkArgument(SANITIZED_ISSUES_DEPRECATED_FOLDER.exists());
//        Preconditions.checkArgument(SANITIZED_ISSUES_DEPRECATED_FOLDER.isDirectory());
//        Preconditions.checkArgument(SANITIZED_ISSUES_MAINSCOPE_FOLDER.exists());
//        Preconditions.checkArgument(SANITIZED_ISSUES_MAINSCOPE_FOLDER.isDirectory());
//        Preconditions.checkArgument(SANITIZED_ISSUES_NEGATIVETESTS_FOLDER.exists());
//        Preconditions.checkArgument(SANITIZED_ISSUES_NEGATIVETESTS_FOLDER.isDirectory());
//    }

}
