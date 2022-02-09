package nz.ac.wgtn.nullannoinference.annotator;


import com.github.javaparser.ast.body.MethodDeclaration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeepPackageTests extends AbstractInjectAnnotationTest {

    @Test
    public void testAnnotationOfReturnType() throws Exception {
        File in = new File(BasicTests.class.getResource("/Class6.java").getFile());
        File out = new File(TMP,"Class6.java");
        NullableSpec spec = new NullableSpec("foo.part1.Class6", "foo","()Ljava/lang/Object;", NullableSpec.Kind.RETURN_VALUE, -1);
        int annotationsInsertedCount = this.annotator.annotateMethod(in,out,Set.of(spec));
        assertEquals(1,annotationsInsertedCount);
        assertTrue(out.exists());

        MethodDeclaration method = findMethod(out,"Class6","foo","()Ljava/lang/Object;");
        Set<String> annotations = method.getAnnotations().stream().map(a -> a.getNameAsString()).collect(Collectors.toSet());
        assertTrue(annotations.contains(annotator.getAnnotationSpec().getNullableAnnotationName()));
    }

}
