package nz.ac.wgtn.nullinference.experiments.spring;

import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.IssueKernel;
import nz.ac.wgtn.nullannoinference.commons.ProjectType;
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

    public static final List<String> FULL_DATASET = Stream.concat(
        SPRING_MODULES.stream(),ADDITIONAL_PROGRAMS.stream()
    ).collect(Collectors.toList());


    public static final ProjectType getProjectType(String name) {
        if (name.equals("guava")) {
            return ProjectType.GUAVA;
        }
        else if (name.equals("error-prone")) {
            return ProjectType.ERROR_PRONE;
        }
        else if (name.startsWith("spring-")) {
            return ProjectType.MULTI_GRADLE;
        }
        throw new IllegalStateException();
    }

    public static final ProjectType getProjectRootFolder(String name) {
        if (name.equals("guava")) {
            return ProjectType.GUAVA;
        }
        else if (name.equals("error-prone")) {
            return ProjectType.ERROR_PRONE;
        }
        else if (name.startsWith("spring-")) {
            return ProjectType.GRADLE;
        }
        throw new IllegalStateException();
    }

    public static final File SPRING_RESULTS = new File("experiments-spring/results/");
    public static final File ADDITIONAL_RESULTS = new File("experiments-additional/results/");

    private static File[] getResultFolders(String name) {
        return new File[] {
            new File(SPRING_RESULTS,name),
            new File(ADDITIONAL_RESULTS,name)
        };
    };

    public static final SearchPath EXTRACTED_ISSUES_FOLDER = SearchPath.of(getResultFolders("extracted"));
    public static final SearchPath EXTRACTED_ISSUES_WITH_VOID_FOLDER = SearchPath.of(getResultFolders("extracted-with-void"));
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

    public static final File SPRING_PROJECTS = new File("experiments-spring/projects/original/spring-framework");
    public static final File ADDITIONAL_PROJECTS = new File("experiments-additional/projects/original/");
    public static final File[] ALL_PROJECTS = new File[]{SPRING_PROJECTS,ADDITIONAL_PROJECTS};

    public static File locateProject(String name) {
        for (File dir:ALL_PROJECTS) {
            assert dir.exists();
            File pr = new File(dir,name);
            if (pr.exists()) {
                return pr;
            }
        }
        throw new IllegalStateException("Project not found: " + name);
    }

    public static final Predicate<Issue> FIELDS_ONLY = issue -> issue.getKind()== Issue.IssueType.FIELD;
    public static final Predicate<Issue> PARAM_ONLY = issue -> issue.getKind()== Issue.IssueType.ARGUMENT;
    public static final Predicate<Issue> RETURNS_ONLY = issue -> issue.getKind()== Issue.IssueType.RETURN_VALUE;

    public static final Predicate<IssueKernel> AGGR_FIELDS_ONLY = issue -> issue.getKind()== Issue.IssueType.FIELD;
    public static final Predicate<IssueKernel> AGGR_PARAM_ONLY = issue -> issue.getKind()== Issue.IssueType.ARGUMENT;
    public static final Predicate<IssueKernel> AGGR_RETURNS_ONLY = issue -> issue.getKind()== Issue.IssueType.RETURN_VALUE;

    public static final Predicate<IssueKernel> AGGRE_ALL = issue -> true;
    public static final Predicate<Issue> ALL = issue -> true;


}
