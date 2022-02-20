package nz.ac.wgtn.nullannoinference.agent2;

import com.ea.agentloader.AgentLoader;
import nz.ac.wgtn.nullannoinference.agent2.data.A;
import nz.ac.wgtn.nullannoinference.agent2.data.B;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.IssueStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestFieldAccess {

    @BeforeAll
    public static void installAgent() {
        AgentLoader.loadAgentClass(NullLoggerAgent.class.getName(),null);
    }

    @BeforeEach
    public void setup() {
        IssueStore.clear();
    }

    @Test
    public void testNonStaticFieldAccessString() {
        new A().resetf1();
        assertEquals(1, IssueStore.getIssues().size());

        Issue issue = IssueStore.getIssues().stream().filter(iss -> iss.getMethodName().equals("f1")).findFirst().orElse(null);
        assertNotNull(issue);
        assertEquals(A.class.getName(),issue.getClassName());
        assertEquals("f1",issue.getMethodName());
        assertEquals("Ljava/lang/String;",issue.getDescriptor());
        assertEquals(-1,issue.getArgsIndex());
        assertEquals(Issue.IssueType.FIELD,issue.getKind());
    }

    @Test
    public void testNonStaticFieldAccessObject() {
        new A().resetf2();
        assertEquals(1, IssueStore.getIssues().size());

        Issue issue = IssueStore.getIssues().stream().filter(iss -> iss.getMethodName().equals("f2")).findFirst().orElse(null);
        assertNotNull(issue);
        assertEquals(A.class.getName(),issue.getClassName());
        assertEquals("f2",issue.getMethodName());
        assertEquals("Ljava/lang/Object;",issue.getDescriptor());
        assertEquals(-1,issue.getArgsIndex());
        assertEquals(Issue.IssueType.FIELD,issue.getKind());
    }

    @Test
    public void testNonStaticFieldAccessArrayOfString() {
        new A().resetf3();
        assertEquals(1, IssueStore.getIssues().size());

        Issue issue = IssueStore.getIssues().stream().filter(iss -> iss.getMethodName().equals("f3")).findFirst().orElse(null);
        assertNotNull(issue);
        assertEquals(A.class.getName(),issue.getClassName());
        assertEquals("f3",issue.getMethodName());
        assertEquals("[Ljava/lang/String;",issue.getDescriptor());
        assertEquals(-1,issue.getArgsIndex());
        assertEquals(Issue.IssueType.FIELD,issue.getKind());
    }

    @Test
    public void testNonStaticFieldAccessArrayOfInt() {
        new A().resetf4();
        assertEquals(1, IssueStore.getIssues().size());

        Issue issue = IssueStore.getIssues().stream().filter(iss -> iss.getMethodName().equals("f4")).findFirst().orElse(null);
        assertNotNull(issue);
        assertEquals(A.class.getName(),issue.getClassName());
        assertEquals("f4",issue.getMethodName());
        assertEquals("[I",issue.getDescriptor());
        assertEquals(-1,issue.getArgsIndex());
        assertEquals(Issue.IssueType.FIELD,issue.getKind());
    }

    @Test
    public void testNonStaticFieldForFP() {
        new A().dontResetf5();
        assertEquals(0, IssueStore.getIssues().size());
    }

    // no oracle needed -- instrumentation would cause verification error as it would result in a corrupt state
    @Test
    public void testNonStaticFieldRead() {
        new A().getF1();
    }

    @Test
    public void testStaticFieldAccessString() {
        B.resetF1();
        assertEquals(1, IssueStore.getIssues().size());

        Issue issue = IssueStore.getIssues().stream().filter(iss -> iss.getMethodName().equals("F1")).findFirst().orElse(null);
        assertNotNull(issue);
        assertEquals(B.class.getName(),issue.getClassName());
        assertEquals("F1",issue.getMethodName());
        assertEquals("Ljava/lang/String;",issue.getDescriptor());
        assertEquals(-1,issue.getArgsIndex());
        assertEquals(Issue.IssueType.FIELD,issue.getKind());
    }

    @Test
    public void testStaticFieldAccessObject() {
        B.resetF2();
        assertEquals(1, IssueStore.getIssues().size());

        Issue issue = IssueStore.getIssues().stream().filter(iss -> iss.getMethodName().equals("F2")).findFirst().orElse(null);
        assertNotNull(issue);
        assertEquals(B.class.getName(),issue.getClassName());
        assertEquals("F2",issue.getMethodName());
        assertEquals("Ljava/lang/Object;",issue.getDescriptor());
        assertEquals(-1,issue.getArgsIndex());
        assertEquals(Issue.IssueType.FIELD,issue.getKind());
    }

    @Test
    public void testStaticFieldAccessArrayOfString() {
        B.resetF3();
        assertEquals(1, IssueStore.getIssues().size());

        Issue issue = IssueStore.getIssues().stream().filter(iss -> iss.getMethodName().equals("F3")).findFirst().orElse(null);
        assertNotNull(issue);
        assertEquals(B.class.getName(),issue.getClassName());
        assertEquals("F3",issue.getMethodName());
        assertEquals("[Ljava/lang/String;",issue.getDescriptor());
        assertEquals(-1,issue.getArgsIndex());
        assertEquals(Issue.IssueType.FIELD,issue.getKind());
    }

    @Test
    public void testStaticFieldAccessArrayOfInt() {
        B.resetF4();
        assertEquals(1, IssueStore.getIssues().size());

        Issue issue = IssueStore.getIssues().stream().filter(iss -> iss.getMethodName().equals("F4")).findFirst().orElse(null);
        assertNotNull(issue);
        assertEquals(B.class.getName(),issue.getClassName());
        assertEquals("F4",issue.getMethodName());
        assertEquals("[I",issue.getDescriptor());
        assertEquals(-1,issue.getArgsIndex());
        assertEquals(Issue.IssueType.FIELD,issue.getKind());
    }

    @Test
    public void testStaticFieldForFP() {
        B.dontResetF5();
        assertEquals(0, IssueStore.getIssues().size());
    }

    // no oracle needed -- instrumentation would cause verification error as it would result in a corrupt state
    @Test
    public void testStaticFieldRead() {
        B.getF1();
    }
}
