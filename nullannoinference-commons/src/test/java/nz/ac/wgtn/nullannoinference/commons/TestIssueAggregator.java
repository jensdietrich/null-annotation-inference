package nz.ac.wgtn.nullannoinference.commons;

import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.IssueAggregator;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestIssueAggregator {

    @Test
    public void test () {
        Issue issue1 = new Issue("A","foo","()Ljava/lang/String","context",Issue.IssueType.ARGUMENT,1);
        issue1.setStacktrace(List.of("foo1","bar1"));

        Issue issue2 = new Issue("A","foo","()Ljava/lang/String","context",Issue.IssueType.ARGUMENT,1);
        issue1.setStacktrace(List.of("foo2","bar2")); // different reason, otherwise same issue

        Issue issue3 = new Issue("B","foo","()Ljava/lang/String","context",Issue.IssueType.ARGUMENT,0);
        issue1.setStacktrace(List.of("foo1","bar1")); // different issue

        Set<Issue> issues = Set.of(issue1,issue2,issue3);

        Collection<IssueKernel> aggregatedSet = IssueAggregator.aggregate(issues);

        assertEquals(2,aggregatedSet.size());

        Collection<IssueKernel> issuesInA = aggregatedSet.stream().filter(i -> i.getClassName().equals("A")).collect(Collectors.toSet());
        assertEquals(1,issuesInA.size());
        assertTrue(issuesInA.contains(issue1.getKernel()) || issuesInA.contains(issue2.getKernel()));

        Collection<IssueKernel> issuesInB = aggregatedSet.stream().filter(i -> i.getClassName().equals("B")).collect(Collectors.toSet());
        assertEquals(1,issuesInB.size());
        assertTrue(issuesInB.contains(issue3.getKernel()));

    }
}
