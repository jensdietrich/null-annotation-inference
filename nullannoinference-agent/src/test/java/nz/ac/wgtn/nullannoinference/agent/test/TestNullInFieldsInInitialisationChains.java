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

public class TestNullInFieldsInInitialisationChains {

    @BeforeEach
    public void setup() {
        Dynamic.transform(FooSuper.class);
        Dynamic.transform(FooSub.class);
        IssueStore.clear();
    }

    @AfterEach
    public void tearDown() {
        IssueStore.clear();
    }

    @Test
    public void test() {

        FooSub sub = new FooSub();
        System.out.println(sub);
        List<Issue> fieldIssues = IssueStore.getIssues().stream()
            .filter(issue -> issue.getKind()== Issue.IssueType.FIELD)
            .collect(Collectors.toList());

        // must not list f1 -- this is a FP caused by the check when FooSuper::<init> exits
        assertEquals(1,fieldIssues.size());

        Issue issuef2= IssueStore.getIssues().stream().filter(issue -> issue.getMethodName().equals("f2")).findFirst().orElse(null);
        assertNotNull(issuef2);

    }

}
