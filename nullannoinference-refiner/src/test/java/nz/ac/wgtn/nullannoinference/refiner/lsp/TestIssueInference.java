package nz.ac.wgtn.nullannoinference.refiner.lsp;

import com.google.common.collect.Sets;
import com.google.common.graph.Graph;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class TestIssueInference {

    @Test
    public void testNullableArgumentPropagatedDownWithPropagateNullabilityInArgumentsOn() throws IOException {

        Issue issue = new Issue("foo.B","foo","(Ljava/lang/Object;)Ljava/lang/String;", null,Issue.IssueType.ARGUMENT,0);
        File project = new File(TestOverrideExtractor.class.getResource("/test-project").getFile());
        assumeTrue(project.exists());
        assumeTrue(new File(project, "target/classes").exists(), "project containing test data (resources/test-project) has not been built, build test projects with mvn compile");
        Graph<OwnedMethod> overrides = OverrideExtractor.extractOverrides(t -> t.startsWith("foo."),project);

        Set<Issue> newIssues = InferAdditionalIssues.inferIssuesViaLSPPropagation(Sets.newHashSet(issue),overrides,true);

        assertEquals(1,newIssues.size());
        Issue newIssue = newIssues.iterator().next();
        assertEquals("foo.C",newIssue.getClassName());
        assertEquals(issue.getMethodName(),newIssue.getMethodName());
        assertEquals(issue.getDescriptor(),newIssue.getDescriptor());
        assertEquals(0,newIssue.getArgsIndex());
        assertEquals(Issue.IssueType.ARGUMENT,newIssue.getKind());
        assertEquals(Issue.ProvenanceType.INFERRED,newIssue.getProvenanceType());
        assertEquals(issue,newIssue.getParent());
    }

    @Test
    public void testNullableArgumentPropagatedDownWithPropagateNullabilityInArgumentsOff() throws IOException {

        Issue issue = new Issue("foo.B","foo","(Ljava/lang/Object;)Ljava/lang/String;", null,Issue.IssueType.ARGUMENT,0);
        File project = new File(TestOverrideExtractor.class.getResource("/test-project").getFile());
        assumeTrue(project.exists());
        assumeTrue(new File(project, "target/classes").exists(), "project containing test data (resources/test-project) has not been built, build test projects with mvn compile");
        Graph<OwnedMethod> overrides = OverrideExtractor.extractOverrides(t -> t.startsWith("foo."),project);

        Set<Issue> newIssues = InferAdditionalIssues.inferIssuesViaLSPPropagation(Sets.newHashSet(issue),overrides,false);

        assertEquals(0,newIssues.size());
    }

    @Test
    public void testNullableReturnPropagatedUpWithPropagateNullabilityInArgumentsOn() throws IOException {

        Issue issue = new Issue("foo.B","foo","(Ljava/lang/Object;)Ljava/lang/String;", null,Issue.IssueType.RETURN_VALUE,-1);
        File project = new File(TestOverrideExtractor.class.getResource("/test-project").getFile());
        assumeTrue(project.exists());
        assumeTrue(new File(project, "target/classes").exists(), "tested project has not been built, build test projects with mvn compile");
        Graph<OwnedMethod> overrides = OverrideExtractor.extractOverrides(t -> t.startsWith("foo."),project);

        Set<Issue> newIssues = InferAdditionalIssues.inferIssuesViaLSPPropagation(Sets.newHashSet(issue),overrides,true);

        assertEquals(1,newIssues.size());
        Issue newIssue = newIssues.iterator().next();
        assertEquals("foo.A",newIssue.getClassName());
        assertEquals(issue.getMethodName(),newIssue.getMethodName());
        assertEquals(issue.getDescriptor(),newIssue.getDescriptor());
        assertEquals(-1,newIssue.getArgsIndex());
        assertEquals(Issue.IssueType.RETURN_VALUE,newIssue.getKind());
        assertEquals(Issue.ProvenanceType.INFERRED,newIssue.getProvenanceType());
        assertEquals(issue,newIssue.getParent());
    }

    @Test
    public void testNullableReturnPropagatedUpWithPropagateNullabilityInArgumentsOff() throws IOException {

        Issue issue = new Issue("foo.B","foo","(Ljava/lang/Object;)Ljava/lang/String;", null,Issue.IssueType.RETURN_VALUE,-1);
        File project = new File(TestOverrideExtractor.class.getResource("/test-project").getFile());
        assumeTrue(project.exists());
        assumeTrue(new File(project, "target/classes").exists(), "tested project has not been built, build test projects with mvn compile");
        Graph<OwnedMethod> overrides = OverrideExtractor.extractOverrides(t -> t.startsWith("foo."),project);

        Set<Issue> newIssues = InferAdditionalIssues.inferIssuesViaLSPPropagation(Sets.newHashSet(issue),overrides,false);

        assertEquals(1,newIssues.size());
        Issue newIssue = newIssues.iterator().next();
        assertEquals("foo.A",newIssue.getClassName());
        assertEquals(issue.getMethodName(),newIssue.getMethodName());
        assertEquals(issue.getDescriptor(),newIssue.getDescriptor());
        assertEquals(-1,newIssue.getArgsIndex());
        assertEquals(Issue.IssueType.RETURN_VALUE,newIssue.getKind());
        assertEquals(Issue.ProvenanceType.INFERRED,newIssue.getProvenanceType());
        assertEquals(issue,newIssue.getParent());
    }
}
