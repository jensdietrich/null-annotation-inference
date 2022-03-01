package nz.ac.wgtn.nullannoinference.agent.test;

import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.IssueStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestNullInFieldsAfterObjectInitialisation {

    @BeforeEach
    public void setup() {
        Dynamic.transform(FooField.class);
        IssueStore.clear();
    }

    @AfterEach
    public void tearDown() {
        IssueStore.clear();
    }

    @Test
    public void test1() {
        FooField foo = new FooField(null,"not-null","not-null",-1, new int[]{});
        List<Issue> fieldIssues = IssueStore.getIssues().stream().filter(issue -> issue.getKind()== Issue.IssueType.FIELD).collect(Collectors.toList());

        assertEquals(1,fieldIssues.size());

        Issue issuef1 = IssueStore.getIssues().stream().filter(issue -> issue.getMethodName().equals("f1")).findFirst().orElse(null);
        assertNotNull(issuef1);
    }

    @Test
    public void test2() {
        FooField foo = new FooField();
        List<Issue> fieldIssues = IssueStore.getIssues().stream().filter(issue -> issue.getKind() == Issue.IssueType.FIELD).collect(Collectors.toList());

        assertEquals(2,fieldIssues.size());

        Issue issuef1 = IssueStore.getIssues().stream().filter(issue -> issue.getMethodName().equals("f1")).findFirst().orElse(null);
        assertNotNull(issuef1);

        Issue issuef5 = IssueStore.getIssues().stream().filter(issue -> issue.getMethodName().equals("f5")).findFirst().orElse(null);
        assertNotNull(issuef5);
    }

    @Test
    public void test3() {
        FooField foo = new FooField(null,null,null,-1,null);
        List<Issue> fieldIssues = IssueStore.getIssues().stream().filter(issue -> issue.getKind()== Issue.IssueType.FIELD).collect(Collectors.toList());

        assertEquals(4,fieldIssues.size());

        Issue issuef1= IssueStore.getIssues().stream().filter(issue -> issue.getMethodName().equals("f1")).findFirst().orElse(null);
        assertNotNull(issuef1);

        Issue issuef2= IssueStore.getIssues().stream().filter(issue -> issue.getMethodName().equals("f2")).findFirst().orElse(null);
        assertNotNull(issuef2);

        Issue issuef3= IssueStore.getIssues().stream().filter(issue -> issue.getMethodName().equals("f3")).findFirst().orElse(null);
        assertNotNull(issuef3);

        Issue issuef5= IssueStore.getIssues().stream().filter(issue -> issue.getMethodName().equals("f5")).findFirst().orElse(null);
        assertNotNull(issuef5);
    }
}
