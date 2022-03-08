package nz.ac.wgtn.nullannoinference.annotator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.File;

// issue encountered during development (not mapped to issue tracker)
public class Issue1Test {

    public static File TMP = new File(".tmp");

    @BeforeAll
    public static void globalSetup() {
        if (!TMP.exists()) {
            TMP.mkdirs();
        }
    }

    @Test
    public void test() throws Exception {
        File in = new File(Issue1Test.class.getResource("/issue1/").getFile());
        File out = new File(TMP,"issue1/");
        File issueFolder = new File(Issue1Test.class.getResource("/issue1/").getFile());
        MvnProjectAnnotator.main(new String[] {
                "-"+MvnProjectAnnotator.ARG_INPUT,in.getAbsolutePath(),
                "-"+MvnProjectAnnotator.ARG_OUTPUT,out.getAbsolutePath(),
                "-"+MvnProjectAnnotator.ARG_ISSUES,issueFolder.getAbsolutePath(),
                "-"+MvnProjectAnnotator.ARG_PROJECT_NAME,"mock-foo"
        });
    }
}
