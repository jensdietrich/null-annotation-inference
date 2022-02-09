package nz.ac.wgtn.nullannoinference.agent.test;

import nz.ac.wgtn.nullannoinference.agent.Issue;
import nz.ac.wgtn.nullannoinference.agent.IssueStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestNullInReturnValues {

    @BeforeEach
    public void setup() {
        Dynamic.transform(FooReturn.class);
        IssueStore.clear();
    }

    @AfterEach
    public void tearDown() {
        IssueStore.clear();
    }

    @Test
    public void test() {
        FooReturn.main(new String[]{});
        assertEquals(1,IssueStore.getIssues().size());

        Issue issueM2 = IssueStore.getIssues().stream().filter(iss -> iss.getMethodName().equals("m2")).findFirst().orElse(null);
        assertNotNull(issueM2);
        assertEquals(FooReturn.class.getName(),issueM2.getClassName());
        assertEquals("m2",issueM2.getMethodName());
        assertEquals("(Ljava/lang/Object;)Ljava/lang/Object;",issueM2.getDescriptor());
        assertEquals(-1,issueM2.getArgsIndex());
        assertEquals(Issue.IssueType.RETURN_VALUE,issueM2.getKind());

    }
}
