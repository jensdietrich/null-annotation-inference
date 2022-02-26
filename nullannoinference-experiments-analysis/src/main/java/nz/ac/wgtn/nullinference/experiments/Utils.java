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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Some reusable utilities.
 * @author jens dietrich
 */
public class Utils {

    static List<String> getListOfProjects(File issuesFolder) {
        List<String> projects = Stream.of(issuesFolder.listFiles())
            .filter(f -> f.isDirectory())
            .filter(f -> !f.isHidden())
            .map(f -> f.getName())
            .sorted()
            .collect(Collectors.toList());

        Preconditions.checkState(!projects.isEmpty(),"no project folders found");
        return projects;
    }

    static List<String> getListOfProjectsAndCheckConsistency(File collectedIssuesFolder, File inferredIssuesFolder) {
        List<String> projects1 = Stream.of(collectedIssuesFolder.listFiles())
            .filter(f -> f.isDirectory())
            .filter(f -> !f.isHidden())
            .map(f -> f.getName())
            .sorted()
            .collect(Collectors.toList());

        List<String> projects2 = Stream.of(inferredIssuesFolder.listFiles())
            .filter(f -> f.isDirectory())
            .filter(f -> !f.isHidden())
            .map(f -> f.getName())
            .sorted()
            .collect(Collectors.toList());

        Preconditions.checkState(projects1.equals(projects2),"list of project folders in collected / inferred results root folders does not match");
        Preconditions.checkState(!projects1.isEmpty(),"no project folders found");
        return projects1;
    }

    static Set<Issue> loadIssues(File folder, boolean aggregate, Predicate<Issue>... filters) {
        Set<Issue> issues = new HashSet<>();
        Predicate<Issue> filter = issue -> true;
        for (Predicate<Issue> filter2:filters) {
            filter = filter.and(filter2);
        }
        for (File file: FileUtils.listFiles(folder,new String[]{"json"},true)) {
            System.out.println("reading issues from " + file.getAbsolutePath());
            Gson gson = new Gson();
            try (Reader in = new FileReader(file)) {
                Type listType = new TypeToken<ArrayList<Issue>>() {}.getType();
                List<Issue> issues2 = gson.fromJson(in, listType);
                issues.addAll(issues2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("" + issues.size() + " issues imported");

        issues = issues.stream().filter(filter).collect(Collectors.toSet());
        return aggregate? IssueAggregator.aggregate(issues):issues;
    }

    static NumberFormat INT_FORMAT = new DecimalFormat("###,###,###");
    static String format (int number) {
        return INT_FORMAT.format(number);
    }

}
