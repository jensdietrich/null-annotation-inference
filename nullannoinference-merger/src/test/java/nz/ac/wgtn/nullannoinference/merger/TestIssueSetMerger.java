package nz.ac.wgtn.nullannoinference.merger;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commonsio.IssueIO;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class TestIssueSetMerger {

    @Test
    public void test() throws Exception {
        File resourceFolder = new File(TestIssueSetMerger.class.getResource("/").getFile());
        assumeTrue(resourceFolder.exists());

        File issueFile1 = new File(resourceFolder,"issues1.json");
        File issueFile2 = new File(resourceFolder,"issues2.json");
        assumeTrue(issueFile1.exists());
        assumeTrue(issueFile2.exists());
        File mergedIssueFile = new File(resourceFolder,"merged.json");

        IssueSetMerger.main(new String[]{"-i",issueFile1.getAbsolutePath()+","+issueFile2.getAbsolutePath(),"-o",mergedIssueFile.getAbsolutePath()});

        JsonReader reader = new JsonReader(new FileReader(issueFile1));
        List<Issue> issues1 = IssueIO.readIssues(reader);

        reader = new JsonReader(new FileReader(issueFile2));
        List<Issue> issues2 = IssueIO.readIssues(reader);

        reader = new JsonReader(new FileReader(mergedIssueFile));
        List<Issue> mergedIssues = IssueIO.readIssues(reader);

        assumeTrue(issues1.size()==1);
        assumeTrue(issues2.size()==1);
        assertEquals(2,mergedIssues.size());

        Issue issue1 = issues1.get(0);
        Issue issue2 = issues2.get(0);

        assertEquals(issue1.getClassName(),mergedIssues.get(0).getClassName());
        assertEquals(issue2.getClassName(),mergedIssues.get(1).getClassName());

        assertEquals(issue1.getMethodName(),mergedIssues.get(0).getMethodName());
        assertEquals(issue2.getMethodName(),mergedIssues.get(1).getMethodName());

        assertEquals(issue1.getArgsIndex(),mergedIssues.get(0).getArgsIndex());
        assertEquals(issue2.getArgsIndex(),mergedIssues.get(1).getArgsIndex());

        assertEquals(issue1.getDescriptor(),mergedIssues.get(0).getDescriptor());
        assertEquals(issue2.getDescriptor(),mergedIssues.get(1).getDescriptor());

        assertEquals(issue1.getContext(),mergedIssues.get(0).getContext());
        assertEquals(issue2.getContext(),mergedIssues.get(1).getContext());

        assertEquals(issue1.getKind(),mergedIssues.get(0).getKind());
        assertEquals(issue2.getKind(),mergedIssues.get(1).getKind());

        assertEquals(issue1.getStacktrace(),mergedIssues.get(0).getStacktrace());
        assertEquals(issue2.getStacktrace(),mergedIssues.get(1).getStacktrace());

    }
}
