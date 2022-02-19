package nz.ac.wgtn.nullannoinference.agent.test;

import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.IssueStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestNullInArguments {

    @BeforeEach
    public void setup() {
        Dynamic.transform(FooNoReturn.class);
        IssueStore.clear();
    }

    @AfterEach
    public void tearDown() {
        IssueStore.clear();
    }

    @Test
    public void test() {
        FooNoReturn.main(new String[]{});
        assertEquals(2,IssueStore.getIssues().size());

        Issue issueM2 = IssueStore.getIssues().stream().filter(iss -> iss.getMethodName().equals("m2")).findFirst().orElse(null);
        assertNotNull(issueM2);
        assertEquals(FooNoReturn.class.getName(),issueM2.getClassName());
        assertEquals("m2",issueM2.getMethodName());
        assertEquals("(Ljava/lang/Object;)V",issueM2.getDescriptor());
        assertEquals(0,issueM2.getArgsIndex());
        assertEquals(Issue.IssueType.ARGUMENT,issueM2.getKind());

        Issue issueM3 = IssueStore.getIssues().stream().filter(iss -> iss.getMethodName().equals("m3")).findFirst().orElse(null);
        assertNotNull(issueM3);
        assertEquals(FooNoReturn.class.getName(),issueM3.getClassName());
        assertEquals("m3",issueM3.getMethodName());
        assertEquals("(Ljava/lang/Object;Ljava/lang/Object;)Z",issueM3.getDescriptor());
        assertEquals(1,issueM3.getArgsIndex());
        assertEquals(Issue.IssueType.ARGUMENT,issueM3.getKind());
    }
}
