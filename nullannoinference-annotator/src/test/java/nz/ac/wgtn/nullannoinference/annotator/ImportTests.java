package nz.ac.wgtn.nullannoinference.annotator;


import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.util.Collections;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImportTests extends AbstractInjectAnnotationTest {

    @Test
    public void testCreationOfImport() throws Exception {
        File in = new File(ImportTests.class.getResource("/Class2a.java").getFile());
        File out = new File(TMP,"Class2a.java");
        Issue spec = new Issue("Class2a", "foo","()Ljava/lang/Object;", null, Issue.IssueType.RETURN_VALUE);
        int count = this.annotator.annotateMember(in,out, Set.of(spec), Collections.emptyList());
        assertEquals(1,count);
        assertTrue(out.exists());

        ParseResult<CompilationUnit> result = new JavaParser().parse(out);
        assertTrue(result.isSuccessful());
        CompilationUnit cu = result.getResult().get();
        long annotationImportCount = cu.getImports().stream()
            .filter(imp -> !imp.isStatic())
            .filter(imp -> !imp.isAsterisk())
            .map(imp -> imp.getNameAsString())
            .filter(n -> n.equals(annotator.getAnnotationSpec().getNullableAnnotationQName()))
            .count();
        assertEquals(1,annotationImportCount);
    }


    @Test
    public void testReuseOfExistingClassImport() throws Exception {
        File in = new File(ImportTests.class.getResource("/Class2b.java").getFile());
        File out = new File(TMP,"Class2b.java");
        Issue spec = new Issue("Class2b", "foo","()Ljava/lang/Object;", null,Issue.IssueType.RETURN_VALUE);
        int count = this.annotator.annotateMember(in,out,Set.of(spec),Collections.emptyList());
        assertEquals(1,count);
        assertTrue(out.exists());

        ParseResult<CompilationUnit> result = new JavaParser().parse(out);
        assertTrue(result.isSuccessful());
        CompilationUnit cu = result.getResult().get();
        long annotationImportCount = cu.getImports().stream()
            .filter(imp -> !imp.isStatic())
            .filter(imp -> !imp.isAsterisk())
            .map(imp -> imp.getNameAsString())
            .filter(n -> n.equals(annotator.getAnnotationSpec().getNullableAnnotationQName()))
            .count();
        assertEquals(1,annotationImportCount);
    }

    @Test
    public void testReuseOfExistingPackageImport() throws Exception {
        File in = new File(ImportTests.class.getResource("/Class2c.java").getFile());
        File out = new File(TMP,"Class2c.java");
        Issue spec = new Issue("Class2c", "foo","()Ljava/lang/Object;", null,Issue.IssueType.RETURN_VALUE);
        int count = this.annotator.annotateMember(in,out,Set.of(spec),Collections.emptyList());
        assertEquals(1,count);
        assertTrue(out.exists());

        ParseResult<CompilationUnit> result = new JavaParser().parse(out);
        assertTrue(result.isSuccessful());
        CompilationUnit cu = result.getResult().get();
        long annotationImportCount = cu.getImports().stream()
            .filter(imp -> !imp.isStatic())
            .filter(imp -> imp.isAsterisk())
            .map(imp -> imp.getNameAsString())
            .filter(n -> n.equals(annotator.getAnnotationSpec().getNullableAnnotationPackageName()))
            .count();
        assertEquals(1,annotationImportCount);
    }


}
