package nz.ac.wgtn.nullannoinference.annotator;

import com.github.javaparser.ast.body.MethodDeclaration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class InnerClassesTests extends AbstractInjectAnnotationTest {

    @Test
    public void test1() throws Exception {
        File in = new File(BasicTests.class.getResource("/Class3.java").getFile());
        File out = new File(TMP,"Class3a.java");
        NullableSpec spec = new NullableSpec("Class3$Inner1", "foo","()Ljava/lang/Object;", NullableSpec.Kind.RETURN_VALUE, -1);
        int count = this.annotator.annotateMethod(in,out,Set.of(spec));
        assertEquals(1,count);
        assertTrue(out.exists());

        MethodDeclaration method = findMethod(out,"Class3$Inner1","foo","()Ljava/lang/Object;");
        assertNotNull(method);
        Set<String> annotations = method.getAnnotations().stream().map(a -> a.getNameAsString()).collect(Collectors.toSet());
        assertTrue(annotations.contains(annotator.getAnnotationSpec().getNullableAnnotationName()));

        method = findMethod(out,"Class3$Inner2","foo","()Ljava/lang/Object;");
        assertNotNull(method);
        annotations = method.getAnnotations().stream().map(a -> a.getNameAsString()).collect(Collectors.toSet());
        assertTrue(annotations.isEmpty());

        method = findMethod(out,"Class3$Inner3$Inner31","foo","()Ljava/lang/Object;");
        assertNotNull(method);
        annotations = method.getAnnotations().stream().map(a -> a.getNameAsString()).collect(Collectors.toSet());
        assertTrue(annotations.isEmpty());

        method = findMethod(out,"Class3$Inner3$Inner32","foo","()Ljava/lang/Object;");
        assertNotNull(method);
        annotations = method.getAnnotations().stream().map(a -> a.getNameAsString()).collect(Collectors.toSet());
        assertTrue(annotations.isEmpty());
    }

    @Test
    public void test2() throws Exception {
        File in = new File(BasicTests.class.getResource("/Class3.java").getFile());
        File out = new File(TMP,"Class3b.java");
        NullableSpec spec = new NullableSpec("Class3$Inner2", "foo","()Ljava/lang/Object;", NullableSpec.Kind.RETURN_VALUE, -1);
        int count = this.annotator.annotateMethod(in,out,Set.of(spec));
        assertEquals(1,count);
        assertTrue(out.exists());

        MethodDeclaration method = findMethod(out,"Class3$Inner2","foo","()Ljava/lang/Object;");
        assertNotNull(method);
        Set<String> annotations = method.getAnnotations().stream().map(a -> a.getNameAsString()).collect(Collectors.toSet());
        assertTrue(annotations.contains(annotator.getAnnotationSpec().getNullableAnnotationName()));

        method = findMethod(out,"Class3$Inner1","foo","()Ljava/lang/Object;");
        assertNotNull(method);
        annotations = method.getAnnotations().stream().map(a -> a.getNameAsString()).collect(Collectors.toSet());
        assertTrue(annotations.isEmpty());

        method = findMethod(out,"Class3$Inner3$Inner31","foo","()Ljava/lang/Object;");
        assertNotNull(method);
        annotations = method.getAnnotations().stream().map(a -> a.getNameAsString()).collect(Collectors.toSet());
        assertTrue(annotations.isEmpty());

        method = findMethod(out,"Class3$Inner3$Inner32","foo","()Ljava/lang/Object;");
        assertNotNull(method);
        annotations = method.getAnnotations().stream().map(a -> a.getNameAsString()).collect(Collectors.toSet());
        assertTrue(annotations.isEmpty());
    }

    @Test
    public void test3() throws Exception {
        File in = new File(BasicTests.class.getResource("/Class3.java").getFile());
        File out = new File(TMP,"Class3b.java");
        NullableSpec spec = new NullableSpec("Class3$Inner3$Inner31", "foo","()Ljava/lang/Object;", NullableSpec.Kind.RETURN_VALUE, -1);
        int count = this.annotator.annotateMethod(in,out,Set.of(spec));
        assertEquals(1,count);
        assertTrue(out.exists());

        MethodDeclaration method = findMethod(out,"Class3$Inner3$Inner31","foo","()Ljava/lang/Object;");
        assertNotNull(method);
        Set<String> annotations = method.getAnnotations().stream().map(a -> a.getNameAsString()).collect(Collectors.toSet());
        assertTrue(annotations.contains(annotator.getAnnotationSpec().getNullableAnnotationName()));

        method = findMethod(out,"Class3$Inner1","foo","()Ljava/lang/Object;");
        assertNotNull(method);
        annotations = method.getAnnotations().stream().map(a -> a.getNameAsString()).collect(Collectors.toSet());
        assertTrue(annotations.isEmpty());

        method = findMethod(out,"Class3$Inner1","foo","()Ljava/lang/Object;");
        assertNotNull(method);
        annotations = method.getAnnotations().stream().map(a -> a.getNameAsString()).collect(Collectors.toSet());
        assertTrue(annotations.isEmpty());

        method = findMethod(out,"Class3$Inner3$Inner32","foo","()Ljava/lang/Object;");
        assertNotNull(method);
        annotations = method.getAnnotations().stream().map(a -> a.getNameAsString()).collect(Collectors.toSet());
        assertTrue(annotations.isEmpty());
    }


}
