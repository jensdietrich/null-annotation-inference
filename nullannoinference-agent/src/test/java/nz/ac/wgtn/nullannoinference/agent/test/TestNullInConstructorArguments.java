package nz.ac.wgtn.nullannoinference.agent.test;

import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.IssueStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestNullInConstructorArguments {

    @BeforeEach
    public void setup() {
        Dynamic.transform(FooSuper2.class);
        IssueStore.clear();
    }

    @AfterEach
    public void tearDown() {
        IssueStore.clear();
    }

    @Test
    public void test() {
        new FooSub2("hi","there");
        assertEquals(1,IssueStore.getIssues().size());

        Issue issue = IssueStore.getIssues().stream().findFirst().orElse(null);
        assertNotNull(issue);
        assertEquals(FooSuper2.class.getName(),issue.getClassName());
        assertEquals("<init>",issue.getMethodName());
        assertEquals("(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)",issue.getDescriptor());
        assertEquals(2,issue.getArgsIndex());
        assertEquals(Issue.IssueType.ARGUMENT,issue.getKind());
    }
}
