package nz.ac.wgtn.nullannoinference.annotator;

import com.github.javaparser.ast.body.MethodDeclaration;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnonInnerClassesTests extends AbstractInjectAnnotationTest {

    @Test
    public void test1() throws Exception {
        // files are irrelevant
        File in = new File(BasicTests.class.getResource("/Class4.java").getFile());
        File out = new File(TMP,"Class4a.java");
        Issue spec = new Issue("Class4$Inner$1", "toString","()Ljava/lang/String;", null, Issue.IssueType.RETURN_VALUE);
        int count = annotator.annotateMethod(in,out,Set.of(spec));
        assertTrue(count>0);

        List<MethodDeclaration> methods = findAnoInnerMethods(out,"Class4$Inner$1","toString","()Ljava/lang/String;");

        assertTrue(methods.size()==1);
        MethodDeclaration method = methods.get(0);
        Set<String> annotations = method.getAnnotations().stream().map(a -> a.getNameAsString()).collect(Collectors.toSet());
        assertTrue(annotations.contains(annotator.getAnnotationSpec().getNullableAnnotationName()));

    }

    @Test
    public void test2() throws Exception {
        // files are irrelevant
        File in = new File(BasicTests.class.getResource("/Class4.java").getFile());
        File out = new File(TMP,"Class4b.java");
        Issue spec = new Issue("Class4$2", "compare","(Ljava/lang/String;Ljava/lang/String;)I", null, Issue.IssueType.ARGUMENT, 0);
        int count = annotator.annotateMethod(in,out,Set.of(spec));
        assertTrue(count>0);

        List<MethodDeclaration> methods = findAnoInnerMethods(out,"Class4$2","compare","(Ljava/lang/String;Ljava/lang/String;)I");

        assertTrue(methods.size()==1);
        MethodDeclaration method = methods.get(0);
        Set<String> annotations = method.getParameter(0).getAnnotations().stream().map(a -> a.getNameAsString()).collect(Collectors.toSet());
        assertTrue(annotations.contains(annotator.getAnnotationSpec().getNullableAnnotationName()));

    }

}
