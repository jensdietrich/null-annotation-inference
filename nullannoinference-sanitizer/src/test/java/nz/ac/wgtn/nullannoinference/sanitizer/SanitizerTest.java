package nz.ac.wgtn.nullannoinference.sanitizer;

import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.ProjectType;
import nz.ac.wgtn.nullannoinference.sanitizer.deprecation.DeprecatedElementsSanitizer;
import nz.ac.wgtn.nullannoinference.sanitizer.mainscope.MainScopeSanitizer;
import nz.ac.wgtn.nullannoinference.sanitizer.negtests.Junit4Test;
import nz.ac.wgtn.nullannoinference.sanitizer.negtests.NegativeTestSanitizer;
import nz.ac.wgtn.nullannoinference.sanitizer.shaded.ShadingSanitizer;
import nz.ac.wgtn.nullannoinference.sanitizer.shaded.ShadingSpec;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class SanitizerTest {

    private File project = new File(Junit4Test.class.getResource("/mvn-project").getFile());

    @BeforeEach
    public void setup() {
        Assumptions.assumeTrue(project.exists());
        assumeTrue(new File(project,"target/classes").exists(),"project containing test data (resources/mvn-project) has not been built, build project with \"mvn compile\"");
        assumeTrue(new File(project,"target/test-classes").exists(),"project containing test data (resources/mvn-project) has not been built, build project with \"mvn test-compile\" or \"mvn test\"");
    }

    @Test
    public void testMainScopeSanitizer() {
        MainScopeSanitizer sanitizer = new MainScopeSanitizer(ProjectType.MVN,project);
        Issue issueInMain = new Issue(
            "nz.ac.wgtn.nullannoinference.sanitizer.examples.example1.Class1",
            "m1", "()Ljava/lang/Object;", null,
            Issue.IssueType.RETURN_VALUE);

        Issue issueInTest = new Issue(
            "nz.ac.wgtn.nullannoinference.sanitizer.examples.example1.Test",
            "foo", "()Ljava/lang/Object;", null,
            Issue.IssueType.RETURN_VALUE);

        assertTrue(sanitizer.test(issueInMain));
        assertFalse(sanitizer.test(issueInTest));
    }

    @Test
    public void testDeprecatedElementsSanitizer() throws IOException {
        DeprecatedElementsSanitizer sanitizer = new DeprecatedElementsSanitizer(ProjectType.MVN,project,null);
        Issue issueInDeprecatedMethod1 = new Issue(
            "nz.ac.wgtn.nullannoinference.sanitizer.examples.example1.Class1",
            "m1", "()Ljava/lang/Object;", null,
            Issue.IssueType.RETURN_VALUE);
        Issue issueInDeprecatedMethod2 = new Issue(
            "nz.ac.wgtn.nullannoinference.sanitizer.examples.example1.Class2",
            "m1", "()Ljava/lang/Object;", null,
            Issue.IssueType.RETURN_VALUE);
        Issue issueInDeprecatedClass = new Issue(
            "nz.ac.wgtn.nullannoinference.sanitizer.examples.example1.Class1",
            "m2", "()Ljava/lang/Object;", null,
            Issue.IssueType.RETURN_VALUE);
        Issue issueInNonDeprecated = new Issue(
            "nz.ac.wgtn.nullannoinference.sanitizer.examples.example1.Class2",
            "m2", "()Ljava/lang/Object;", null,
            Issue.IssueType.RETURN_VALUE);

        assertFalse(sanitizer.test(issueInDeprecatedMethod1));
        assertFalse(sanitizer.test(issueInDeprecatedMethod2));
        assertFalse(sanitizer.test(issueInDeprecatedClass));
        assertTrue(sanitizer.test(issueInNonDeprecated));
    }

    @Test
    public void testShadingSanitizer() throws IOException {
        ShadingSpec spec = new ShadingSpec();
        spec.setOriginal("org.example");
        spec.setRenamed("shaded.org.example");
        ShadingSanitizer sanitizer = new ShadingSanitizer(Set.of(spec));

        Issue issueInNonShaded = new Issue(
            "nz.ac.wgtn.nullannoinference.sanitizer.examples.example1.Class1",
            "m1", "()Ljava/lang/Object;", null,
            Issue.IssueType.RETURN_VALUE);

        Issue issueInShaded = new Issue(
            "shaded.org.example.Foo",
            "foo", "()Ljava/lang/Object;", null,
            Issue.IssueType.RETURN_VALUE);

        assertTrue(sanitizer.test(issueInNonShaded));
        assertFalse(sanitizer.test(issueInShaded));
    }

    @Test
    public void testNegativeTestSanitizer() throws IOException {
        NegativeTestSanitizer sanitizer = new NegativeTestSanitizer(ProjectType.MVN,project,null);

        Issue issueCausedByNegativeTest = new Issue(
            "nz.ac.wgtn.nullannoinference.sanitizer.examples.example1.Class1",
            "m1", "()Ljava/lang/Object;", null,
            Issue.IssueType.RETURN_VALUE);
        issueCausedByNegativeTest.setStacktrace(
            List.of("nz.ac.wgtn.nullannoinference.sanitizer.examples.example1.Test1::testNPE:")
        );

        Issue issueNotCausedByNegativeTest = new Issue(
            "shaded.org.example.Foo",
            "foo", "()Ljava/lang/Object;", null,
            Issue.IssueType.RETURN_VALUE);

        assertTrue(sanitizer.test(issueNotCausedByNegativeTest));
        assertFalse(sanitizer.test(issueCausedByNegativeTest));
    }

    @Test
    public void testCompositeSanitizer() throws IOException {
        DeprecatedElementsSanitizer deprecatedElementsSanitizer = new DeprecatedElementsSanitizer(ProjectType.MVN,project,null);
        MainScopeSanitizer mainScopeSanitizer = new MainScopeSanitizer(ProjectType.MVN,project);
        Issue issueInMain = new Issue(
                "nz.ac.wgtn.nullannoinference.sanitizer.examples.example1.Class1",
                "m1", "()Ljava/lang/Object;", null,
                Issue.IssueType.RETURN_VALUE);

        Issue issueInTest = new Issue(
                "nz.ac.wgtn.nullannoinference.sanitizer.examples.example1.Test",
                "foo", "()Ljava/lang/Object;", null,
                Issue.IssueType.RETURN_VALUE);

        Assumptions.assumeTrue(mainScopeSanitizer.test(issueInMain));
        Assumptions.assumeFalse(mainScopeSanitizer.test(issueInTest));

        Assumptions.assumeFalse(deprecatedElementsSanitizer.test(issueInMain));
        Assumptions.assumeTrue(deprecatedElementsSanitizer.test(issueInTest));

        assertTrue(mainScopeSanitizer.or(deprecatedElementsSanitizer).test(issueInMain));
        assertTrue(deprecatedElementsSanitizer.or(mainScopeSanitizer).test(issueInMain));

        assertFalse(mainScopeSanitizer.and(deprecatedElementsSanitizer).test(issueInMain));
        assertFalse(deprecatedElementsSanitizer.and(mainScopeSanitizer).test(issueInMain));

    }


    @Test
    public void testProvenanceForSingleSanitizer () throws IOException {
        DeprecatedElementsSanitizer deprecatedElementsSanitizer = new DeprecatedElementsSanitizer(ProjectType.MVN,project,null);
        MainScopeSanitizer mainScopeSanitizer = new MainScopeSanitizer(ProjectType.MVN,project);
        Issue issueInMain = new Issue(
                "nz.ac.wgtn.nullannoinference.sanitizer.examples.example1.Class1",
                "m1", "()Ljava/lang/Object;", null,
                Issue.IssueType.RETURN_VALUE);

        Issue issueInTest = new Issue(
                "nz.ac.wgtn.nullannoinference.sanitizer.examples.example1.Test",
                "foo", "()Ljava/lang/Object;", null,
                Issue.IssueType.RETURN_VALUE);

        Assumptions.assumeTrue(Sanitizer.sanitize(issueInMain,mainScopeSanitizer));
        assertEquals("true",issueInMain.getProperty(Sanitizer.SANITIZATION_VALUE_KEY));
        assertEquals(mainScopeSanitizer.name(),issueInMain.getProperty(Sanitizer.SANITIZATION_SANITIZER_KEY));

        Assumptions.assumeFalse(Sanitizer.sanitize(issueInTest,mainScopeSanitizer));
        assertEquals("false",issueInTest.getProperty(Sanitizer.SANITIZATION_VALUE_KEY));
        assertEquals(mainScopeSanitizer.name(),issueInTest.getProperty(Sanitizer.SANITIZATION_SANITIZER_KEY));

    }

    @Test
    public void testProvenanceForConjunctiveSanitizer () throws IOException {
        DeprecatedElementsSanitizer deprecatedElementsSanitizer = new DeprecatedElementsSanitizer(ProjectType.MVN,project,null);
        MainScopeSanitizer mainScopeSanitizer = new MainScopeSanitizer(ProjectType.MVN,project);
        Sanitizer<Issue> sanitizer = Sanitizer.ALL.and(deprecatedElementsSanitizer).and(mainScopeSanitizer);
        Issue issueInMain = new Issue(
                "nz.ac.wgtn.nullannoinference.sanitizer.examples.example1.Class1",
                "m1", "()Ljava/lang/Object;", null,
                Issue.IssueType.RETURN_VALUE);


        Assumptions.assumeFalse(Sanitizer.sanitize(issueInMain,sanitizer));
        assertEquals("false",issueInMain.getProperty(Sanitizer.SANITIZATION_VALUE_KEY));
        assertEquals(deprecatedElementsSanitizer.name() + " & " + mainScopeSanitizer.name(),issueInMain.getProperty(Sanitizer.SANITIZATION_SANITIZER_KEY));
    }

    @Test
    public void testProvenanceForDisjunctiveSanitizer () throws IOException {
        DeprecatedElementsSanitizer deprecatedElementsSanitizer = new DeprecatedElementsSanitizer(ProjectType.MVN,project,null);
        MainScopeSanitizer mainScopeSanitizer = new MainScopeSanitizer(ProjectType.MVN,project);
        Sanitizer<Issue> sanitizer = Sanitizer.NONE.or(deprecatedElementsSanitizer).or(mainScopeSanitizer);
        Issue issueInMain = new Issue(
                "nz.ac.wgtn.nullannoinference.sanitizer.examples.example1.Class1",
                "m1", "()Ljava/lang/Object;", null,
                Issue.IssueType.RETURN_VALUE);


        Assumptions.assumeTrue(Sanitizer.sanitize(issueInMain,sanitizer));
        assertEquals("true",issueInMain.getProperty(Sanitizer.SANITIZATION_VALUE_KEY));
        assertEquals(deprecatedElementsSanitizer.name() + " | " + mainScopeSanitizer.name(),issueInMain.getProperty(Sanitizer.SANITIZATION_SANITIZER_KEY));
    }

}
