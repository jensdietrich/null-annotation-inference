package nz.ac.wgtn.nullinference.extractor;

import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.ProjectType;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExtractVoidTest {
    private File project = null;

    @BeforeEach
    public void setup() {
        project = new File(ExtractVoidTest.class.getResource("/void-collector-test-project").getFile());
        Assumptions.assumeTrue(project.exists());
        Assumptions.assumeTrue(new File(project,"target/classes").exists(),"project used for testing must be built first to generate byte code");
    }

    @Test
    public void testIssueCount() {
        Set<Issue> issues = ExtractVoid.findNullAnnotated(ProjectType.MVN,project);
        assertEquals(
                4,
                issues.stream()
                    .filter(issue -> issue.getClassName().equals("nz.ac.wgtn.nullinference.extractor.Foo"))
                    .count()
        );
    }
    @Test
    public void testMethodArg() {
        Set<Issue> issues = ExtractVoid.findNullAnnotated(ProjectType.MVN,project);
        assertEquals(
1,
        issues.stream()
            .filter(issue -> issue.getClassName().equals("nz.ac.wgtn.nullinference.extractor.Foo"))
            .filter(issue -> issue.getMethodName().equals("m1"))
            .filter(issue -> issue.getDescriptor().equals("(Ljava/lang/String;Ljava/lang/Void;)Ljava/lang/Void;"))
            .filter(issue -> issue.getKind() == Issue.IssueType.ARGUMENT)
            .filter(issue -> issue.getArgsIndex() == 1)
            .filter(issue -> issue.getProvenanceType()== Issue.ProvenanceType.EXTRACTED)
            .filter(issue -> issue.getProperty(ExtractNullableAnnotations.PROPERTY_NULLABLE_ANNOTATION_TYPE).equals("java.lang.Void"))
            .count()
        );
    }

    @Test
    public void testMethodReturn() {
        Set<Issue> issues = ExtractVoid.findNullAnnotated(ProjectType.MVN,project);
        assertEquals(
    1,
            issues.stream()
                .filter(issue -> issue.getClassName().equals("nz.ac.wgtn.nullinference.extractor.Foo"))
                .filter(issue -> issue.getMethodName().equals("m1"))
                .filter(issue -> issue.getDescriptor().equals("(Ljava/lang/String;Ljava/lang/Void;)Ljava/lang/Void;"))
                .filter(issue -> issue.getKind() == Issue.IssueType.RETURN_VALUE)
                .filter(issue -> issue.getArgsIndex() == -1)
                .filter(issue -> issue.getProvenanceType()== Issue.ProvenanceType.EXTRACTED)
                .filter(issue -> issue.getProperty(ExtractNullableAnnotations.PROPERTY_NULLABLE_ANNOTATION_TYPE).equals("java.lang.Void"))
                .count()
        );
    }

    @Test
    public void testField() {
        Set<Issue> issues = ExtractVoid.findNullAnnotated(ProjectType.MVN,project);
        assertEquals(
    1,
            issues.stream()
                .filter(issue -> issue.getClassName().equals("nz.ac.wgtn.nullinference.extractor.Foo"))
                .filter(issue -> issue.getMethodName().equals("field1"))
                .filter(issue -> issue.getDescriptor().equals("Ljava/lang/Void;"))
                .filter(issue -> issue.getKind() == Issue.IssueType.FIELD)
                .filter(issue -> issue.getArgsIndex() == -1)
                .filter(issue -> issue.getProvenanceType()== Issue.ProvenanceType.EXTRACTED)
                .filter(issue -> issue.getProperty(ExtractNullableAnnotations.PROPERTY_NULLABLE_ANNOTATION_TYPE).equals("java.lang.Void"))
                .count()
        );
    }

    @Test
    public void testConstructorArg() {
        Set<Issue> issues = ExtractVoid.findNullAnnotated(ProjectType.MVN,project);
        assertEquals(
                1,
                issues.stream()
                        .filter(issue -> issue.getClassName().equals("nz.ac.wgtn.nullinference.extractor.Foo"))
                        .filter(issue -> issue.getMethodName().equals("<init>"))
                        .filter(issue -> issue.getDescriptor().equals("(Ljava/lang/Void;Ljava/lang/String;)"))
                        .filter(issue -> issue.getKind() == Issue.IssueType.ARGUMENT)
                        .filter(issue -> issue.getArgsIndex() == 0)
                        .filter(issue -> issue.getProvenanceType()== Issue.ProvenanceType.EXTRACTED)
                        .filter(issue -> issue.getProperty(ExtractNullableAnnotations.PROPERTY_NULLABLE_ANNOTATION_TYPE).equals("java.lang.Void"))
                        .count()
        );
    }

}
