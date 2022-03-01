package nz.ac.wgtn.nullannoinference.agent.test;

import nz.ac.wgtn.nullannoinference.agent.NullChecks;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.IssueStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestNullInFieldsAfterClassInitialisation {

    @BeforeEach
    public void setup() {
        Dynamic.transform(FooStatic.class);
        IssueStore.clear();
    }

    @AfterEach
    public void tearDown() {
        IssueStore.clear();
    }

    @Test
    public void test() {
        FooStatic.foo();
//        FooStatic foo = new FooStatic();
        List<Issue> fieldIssues = IssueStore.getIssues().stream()
            .filter(issue -> issue.getKind()== Issue.IssueType.FIELD)
            .collect(Collectors.toList());

        assertEquals(2,fieldIssues.size());

        Issue issueF1 = IssueStore.getIssues().stream().filter(issue -> issue.getMethodName().equals("F1")).findFirst().orElse(null);
        assertNotNull(issueF1);

        Issue issueF2 = IssueStore.getIssues().stream().filter(issue -> issue.getMethodName().equals("F2")).findFirst().orElse(null);
        assertNotNull(issueF2);


    }

}
