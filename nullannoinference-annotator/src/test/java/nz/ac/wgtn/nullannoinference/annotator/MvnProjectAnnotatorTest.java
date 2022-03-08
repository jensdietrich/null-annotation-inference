package nz.ac.wgtn.nullannoinference.annotator;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileReader;
import static org.junit.jupiter.api.Assertions.*;

public class MvnProjectAnnotatorTest {

    public static File TMP = new File(".tmp");

    @BeforeAll
    public static void globalSetup() {
        if (!TMP.exists()) {
            TMP.mkdirs();
        }
    }
    // dont clean up for inspection

    @Test
    public void test() throws Exception {
        File in = new File(MvnProjectAnnotatorTest.class.getResource("/project").getFile());
        File out = new File(TMP,"project");
        File nullableSpec = new File(MvnProjectAnnotatorTest.class.getResource("/issues").getFile());
        MvnProjectAnnotator.main(new String[] {
                "-"+MvnProjectAnnotator.ARG_INPUT,in.getAbsolutePath(),
                "-"+MvnProjectAnnotator.ARG_OUTPUT,out.getAbsolutePath(),
                "-"+MvnProjectAnnotator.ARG_ISSUES,nullableSpec.getAbsolutePath(),
                "-"+MvnProjectAnnotator.ARG_PROJECT_NAME,"mock-foo"
        });

        assertTrue(new File(TMP,"project/src/main/java/nz/ac/wgtn/nullannoinference/annotator/testdata1/Main.java").exists());
        assertTrue(new File(TMP,"project/src/main/java/nz/ac/wgtn/nullannoinference/annotator/testdata1/Main2.java").exists());
        assertTrue(new File(TMP,"project/pom.xml").exists());
        assertTrue(new File(TMP,"project/src/main/resources/test.txt").exists());

        // compare content
        String mainOld = IOUtils.toString(new FileReader(new File(in,"src/main/java/nz/ac/wgtn/nullannoinference/annotator/testdata1/Main.java")));
        String main2Old = IOUtils.toString(new FileReader(new File(in,"src/main/java/nz/ac/wgtn/nullannoinference/annotator/testdata1/Main2.java")));
        String classWithFieldsOld = IOUtils.toString(new FileReader(new File(in,"src/main/java/nz/ac/wgtn/nullannoinference/annotator/testdata1/ClassWithFields.java")));
        String pomOld = IOUtils.toString(new FileReader(new File(in,"pom.xml")));
        String resourceOld = IOUtils.toString(new FileReader(new File(in,"src/main/resources/test.txt")));

        // compare content
        String mainNew = IOUtils.toString(new FileReader(new File(TMP,"project/src/main/java/nz/ac/wgtn/nullannoinference/annotator/testdata1/Main.java")));
        String main2New = IOUtils.toString(new FileReader(new File(TMP,"project/src/main/java/nz/ac/wgtn/nullannoinference/annotator/testdata1/Main2.java")));
        String classWithFieldsNew = IOUtils.toString(new FileReader(new File(TMP,"project/src/main/java/nz/ac/wgtn/nullannoinference/annotator/testdata1/ClassWithFields.java")));
        String pomNew = IOUtils.toString(new FileReader(new File(TMP,"project/pom.xml")));
        String resourceNew= IOUtils.toString(new FileReader(new File(TMP,"project/src/main/resources/test.txt")));

        // not changed
        assertEquals(main2Old,main2New);

        // annotated
        assertNotEquals(mainOld,mainNew);
        assertNotEquals(classWithFieldsOld,classWithFieldsNew);

        // annotated
        assertNotEquals(pomOld,pomNew);

        // not changes
        assertEquals(resourceOld,resourceNew);

        // check for some changes (superficially, see other tests for more detailed checks)
        assertTrue(pomNew.contains(MvnProjectAnnotator.DEFAULT_ANNOTATION_PROVIDER.getNullableAnnotationArtifactGroupId()));
        assertTrue(pomNew.contains(MvnProjectAnnotator.DEFAULT_ANNOTATION_PROVIDER.getNullableAnnotationArtifactId()));
        assertTrue(pomNew.contains(MvnProjectAnnotator.DEFAULT_ANNOTATION_PROVIDER.getNullableAnnotationArtifactVersion()));

    }

}
