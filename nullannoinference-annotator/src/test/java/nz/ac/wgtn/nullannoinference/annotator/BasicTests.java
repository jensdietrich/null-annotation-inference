package nz.ac.wgtn.nullannoinference.annotator;

import com.github.javaparser.ast.body.MethodDeclaration;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BasicTests extends AbstractInjectAnnotationTest {


    @Test
    public void testAnnotationOfReturnType() throws Exception {
        File in = new File(BasicTests.class.getResource("/Class1.java").getFile());
        File out = new File(TMP,"Class1a.java");
        Issue spec = new Issue("Class1", "foo","(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", null ,Issue.IssueType.RETURN_VALUE);
        int count = this.annotator.annotateMethods(in,out,Set.of(spec), Collections.EMPTY_LIST);
        assertEquals(1,count);
        assertTrue(out.exists());

        MethodDeclaration method = findMethod(out,"Class1","foo","(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
        Set<String> annotations = method.getAnnotations().stream().map(a -> a.getNameAsString()).collect(Collectors.toSet());
        assertTrue(annotations.contains(annotator.getAnnotationSpec().getNullableAnnotationName()));
    }

    @Test
    public void testAnnotationOfArg0Type() throws Exception {
        File in = new File(BasicTests.class.getResource("/Class1.java").getFile());
        File out = new File(TMP,"Class1b.java");
        Issue spec = new Issue("Class1", "foo","(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", null,Issue.IssueType.ARGUMENT, 0);
        int count = annotator.annotateMethods(in,out,Set.of(spec), Collections.EMPTY_LIST);
        assertEquals(1,count);
        assertTrue(out.exists());

        MethodDeclaration method = findMethod(out,"Class1","foo","(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
        Set<String> annotations = method.getParameter(0).getAnnotations().stream().map(a -> a.getNameAsString()).collect(Collectors.toSet());
        assertTrue(annotations.contains(annotator.getAnnotationSpec().getNullableAnnotationName()));
    }

    @Test
    public void testAnnotationOfArg1Type() throws Exception {
        File in = new File(BasicTests.class.getResource("/Class1.java").getFile());
        File out = new File(TMP,"Class1c.java");
        Issue spec = new Issue("Class1", "foo","(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",null,Issue.IssueType.ARGUMENT, 1);
        int count = annotator.annotateMethods(in,out,Set.of(spec), Collections.EMPTY_LIST);
        assertEquals(1,count);
        assertTrue(out.exists());

        MethodDeclaration method = findMethod(out,"Class1","foo","(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
        Set<String> annotations = method.getParameter(1).getAnnotations().stream().map(a -> a.getNameAsString()).collect(Collectors.toSet());
        assertTrue(annotations.contains(annotator.getAnnotationSpec().getNullableAnnotationName()));
    }

    @Test
    public void testAnnotationOfAllArgAndReturnTypes() throws Exception {
        File in = new File(BasicTests.class.getResource("/Class1.java").getFile());
        File out = new File(TMP,"Class1d.java");
        Issue spec1 = new Issue("Class1", "foo","(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", null, Issue.IssueType.ARGUMENT, 0);
        Issue spec2 = new Issue("Class1", "foo","(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", null, Issue.IssueType.ARGUMENT, 1);
        Issue spec3 = new Issue("Class1", "foo","(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",null,  Issue.IssueType.RETURN_VALUE);

        int count = annotator.annotateMethods(in,out,Set.of(spec1,spec2,spec3), Collections.EMPTY_LIST);
        assertEquals(3,count);
        assertTrue(out.exists());

        MethodDeclaration method = findMethod(out,"Class1","foo","(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");

        Set<String> annotations = method.getAnnotations().stream().map(a -> a.getNameAsString()).collect(Collectors.toSet());
        assertTrue(annotations.contains(annotator.getAnnotationSpec().getNullableAnnotationName()));

        annotations = method.getParameter(0).getAnnotations().stream().map(a -> a.getNameAsString()).collect(Collectors.toSet());
        assertTrue(annotations.contains(annotator.getAnnotationSpec().getNullableAnnotationName()));

        annotations = method.getParameter(1).getAnnotations().stream().map(a -> a.getNameAsString()).collect(Collectors.toSet());
        assertTrue(annotations.contains(annotator.getAnnotationSpec().getNullableAnnotationName()));

    }
}
