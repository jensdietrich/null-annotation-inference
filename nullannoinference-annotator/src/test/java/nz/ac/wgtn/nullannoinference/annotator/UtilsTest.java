package nz.ac.wgtn.nullannoinference.annotator;

import org.junit.jupiter.api.Test;
import java.io.File;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilsTest {
    @Test
    public void testFileNameMatching() {
        File folder = new File(BasicTests.class.getResource("/project").getFile());
        String pattern = folder.getAbsolutePath() + File.separator + "*.xml";
        List<File> files = Utils.getFiles(pattern);
        assertEquals(1,files.size());
        assertEquals("pom.xml",files.get(0).getName());
    }
}
