package nz.ac.wgtn.nullannoinference.annotator;

import com.github.javaparser.ast.body.MethodDeclaration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BasicTests extends AbstractInjectAnnotationTest {


    @Test
    public void testAnnotationOfReturnType() throws Exception {
        File in = new File(BasicTests.class.getResource("/Class1.java").getFile());
        File out = new File(TMP,"Class1a.java");
        NullableSpec spec = new NullableSpec("Class1", "foo","(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", NullableSpec.Kind.RETURN_VALUE, -1);
        int count = this.annotator.annotateMethod(in,out,Set.of(spec));
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
        NullableSpec spec = new NullableSpec("Class1", "foo","(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", NullableSpec.Kind.ARGUMENT, 0);
        int count = annotator.annotateMethod(in,out,Set.of(spec));
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
        NullableSpec spec = new NullableSpec("Class1", "foo","(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", NullableSpec.Kind.ARGUMENT, 1);
        int count = annotator.annotateMethod(in,out,Set.of(spec));
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
        NullableSpec spec1 = new NullableSpec("Class1", "foo","(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", NullableSpec.Kind.ARGUMENT, 0);
        NullableSpec spec2 = new NullableSpec("Class1", "foo","(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", NullableSpec.Kind.ARGUMENT, 1);
        NullableSpec spec3 = new NullableSpec("Class1", "foo","(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", NullableSpec.Kind.RETURN_VALUE, -1);

        int count = annotator.annotateMethod(in,out,Set.of(spec1,spec2,spec3));
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
