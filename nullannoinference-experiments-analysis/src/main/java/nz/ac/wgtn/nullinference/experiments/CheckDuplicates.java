package nz.ac.wgtn.nullinference.experiments;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.IssueAggregator;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Load issues stored in a photo and aggregate them to see whether there are duplicated.
 * @author jens dietrich
 */
public class CheckDuplicates {

    public static void main (String[] args) throws Exception {
        Preconditions.checkArgument(args.length>0,"one argument required -- the result folder");
        File resultFolder = new File(args[0]);
        Preconditions.checkState(resultFolder.exists(),"folder does not exits: " + resultFolder.getAbsolutePath());
        Preconditions.checkState(resultFolder.isDirectory(),"folder must be folder: " + resultFolder.getAbsolutePath());

        Set<Issue> issues = new HashSet<>();
        for (File file: FileUtils.listFiles(resultFolder,new String[]{"json"},true)) {
            System.out.println("Processing " + file.getAbsolutePath());
            Gson gson = new Gson();
            try (Reader in = new FileReader(file)) {
                Type listType = new TypeToken<ArrayList<Issue>>() {}.getType();
                List<Issue> issues2 = gson.fromJson(in, listType);
                issues.addAll(issues2);
                System.out.println("" + issues.size() + " issues imported in total");
            }
        }

        Set<Issue> aggregatedIssues = IssueAggregator.aggregate(issues);
        System.out.println("" + aggregatedIssues.size() + " issues when aggregated");


    }
}
