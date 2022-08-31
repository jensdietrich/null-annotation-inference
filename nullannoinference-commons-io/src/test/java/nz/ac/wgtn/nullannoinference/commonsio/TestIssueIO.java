package nz.ac.wgtn.nullannoinference.commonsio;

import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.IssueKernel;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class TestIssueIO {

    @Test
    public void testReadIssues () throws IOException {
        File issueFile = new File(TestIssueIO.class.getResource("/issues.json").getFile());
        assumeTrue(issueFile.exists());

        List<Issue> issues = IssueIO.readIssues(issueFile);
        assertEquals(3,issues.size());
        checkFirstIssue(issues.get(0));
        checkSecondIssue(issues.get(1));
        checkThirdIssue(issues.get(2));

    }

    @Test
    public void testReadAndAggregateIssues () throws IOException {
        File issueFile = new File(TestIssueIO.class.getResource("/issues.json").getFile());
        assumeTrue(issueFile.exists());

        Map<IssueKernel,Integer> aggregation = IssueIO.readAndAggregateIssues(issueFile);
        assertEquals(2,aggregation.size());

        // compare classes through instances
        List<Issue> issues = IssueIO.readIssues(issueFile);
        assertEquals(3,issues.size());
        Issue issue1 = issues.get(0);
        Issue issue2 = issues.get(1);
        Issue issue3 = issues.get(2);
        IssueKernel kernel1 = issue1.getKernel();
        IssueKernel kernel2 = issue2.getKernel();
        IssueKernel kernel3 = issue3.getKernel();

        assertTrue(kernel1.equals(kernel2));
        assertFalse(kernel1.equals(kernel3));
        assertFalse(kernel2.equals(kernel3));

        assertTrue(aggregation.containsKey(kernel1));
        assertTrue(aggregation.containsKey(kernel3));

        assertEquals(2,aggregation.get(kernel1));
        assertEquals(1,aggregation.get(kernel3));

    }

    private void checkFirstIssue(Issue issue1) {
        assertEquals(Issue.ProvenanceType.OBSERVED,issue1.getProvenanceType());
        assertEquals(List.of("Foo::m1:1","Foo::m2:2"),issue1.getStacktrace());
        assertEquals(Issue.IssueType.RETURN_VALUE,issue1.getKind());
        assertEquals(Issue.Scope.MAIN,issue1.getScope());
        assertEquals(-1,issue1.getArgsIndex());
        assertEquals("com.myprogram.Foo",issue1.getClassName());
        assertEquals("()Ljava/lang/Object;",issue1.getDescriptor());
        assertEquals("foo",issue1.getMethodName());
        assertEquals("myprogram",issue1.getContext());
    }

    private void checkSecondIssue(Issue issue2) {
        assertEquals(Issue.ProvenanceType.OBSERVED,issue2.getProvenanceType());
        assertEquals(List.of("Foo::m1:1","Foo::m2:3"),issue2.getStacktrace());
        assertEquals(Issue.IssueType.RETURN_VALUE,issue2.getKind());
        assertEquals(Issue.Scope.MAIN,issue2.getScope());
        assertEquals(-1,issue2.getArgsIndex());
        assertEquals("com.myprogram.Foo",issue2.getClassName());
        assertEquals("()Ljava/lang/Object;",issue2.getDescriptor());
        assertEquals("foo",issue2.getMethodName());
        assertEquals("myprogram",issue2.getContext());
    }

    private void checkThirdIssue(Issue issue3) {
        assertEquals(Issue.ProvenanceType.OBSERVED,issue3.getProvenanceType());
        assertEquals(List.of("Foo::m1:1","Foo::m2:3"),issue3.getStacktrace());
        assertEquals(Issue.IssueType.FIELD,issue3.getKind());
        assertEquals(Issue.Scope.TEST,issue3.getScope());
        assertEquals(-1,issue3.getArgsIndex());
        assertEquals("com.myprogram.Foo",issue3.getClassName());
        assertEquals("Ljava/lang/Object;",issue3.getDescriptor());
        assertEquals("bar",issue3.getMethodName());
        assertEquals("myprogram2",issue3.getContext());
    }

    @Test
    public void testApplyFilter () throws IOException {
        File issueFile = new File(TestIssueIO.class.getResource("/issues.json").getFile());
        assumeTrue(issueFile.exists());
        Reader input = new FileReader(issueFile);
        Predicate<Issue> filter = issue -> issue.getKind() == Issue.IssueType.FIELD;
        StringWriter output = new StringWriter();
        IssueIO.applyFilter(input,output,filter);

        Reader input2 = new StringReader(output.toString());
        List<Issue> issues = IssueIO.readIssues(input2);
        assertEquals(1,issues.size());
        Issue issue = issues.get(0);
        checkThirdIssue(issue);
    }
}
