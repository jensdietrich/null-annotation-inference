package nz.ac.wgtn.nullinference.experiments;

import nz.ac.wgtn.nullannoinference.commons.Issue;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class IssueFilters {

    public static final Map<String,String> KNOWN_PROJECT_PREFIXES = new HashMap<>();

    static {
        System.out.println("read know package prefixes for projects");
        File file = new File(IssueFilters.class.getResource("/packageprefixes.csv").getFile());
        assert file.exists();
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            in.lines().forEach(line -> {
                String[] tokens = line.split(",");
                assert tokens.length==2;
                String project = tokens[0];
                String prefix = tokens[1];
                KNOWN_PROJECT_PREFIXES.put(tokens[0],tokens[1]);
                System.out.println("added project prefix for project " + project + " : " + prefix );
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static final Predicate<Issue> RETURN = issue -> issue.getKind() == Issue.IssueType.RETURN_VALUE;
    public static final Predicate<Issue> ARG = issue -> issue.getKind() == Issue.IssueType.ARGUMENT;
    public static final Predicate<Issue> FIELD = issue -> issue.getKind() == Issue.IssueType.FIELD;
    public static final Predicate<Issue> INFERRED = issue -> issue.getProvenanceType() == Issue.ProvenanceType.INFERRED;
    public static final Predicate<Issue> COLLECTED = issue -> issue.getProvenanceType() == Issue.ProvenanceType.COLLECTED;

    // whether an issue has been detected by executinbg a test for which the issue was reported
    public static final Predicate<Issue> THIS_PROJECT = issue -> {
       String project = issue.getContext();
       String prefix = KNOWN_PROJECT_PREFIXES.get(project);
       assert prefix!=null;
       return issue.getClassName().startsWith(prefix);
    };
}
