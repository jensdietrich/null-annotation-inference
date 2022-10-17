package nz.ac.wgtn.nullannoinference.commonsio;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import nz.ac.wgtn.nullannoinference.commons.AbstractIssue;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.IssueKernel;
import java.io.*;
import java.util.*;
import java.util.function.Predicate;

/**
 * UI utilities. Many of them are performance optimised to deal with large data.
 * @author jens dietrich
 */
public class IssueIO {

    private static TypeAdapter<Issue> ISSUE_TYPE_ADAPTER = new Gson().getAdapter(Issue.class);

    /**
     * Filter issues from input and write them to output.
     * @param input
     * @param output
     * @param filter
     */
    public static void applyFilter(File input, File output, Predicate<Issue> filter) throws IOException {
        try (Reader reader = new FileReader(input); Writer writer = new FileWriter(output);) {
            applyFilter(reader,writer,filter);
        }
    }

    public static void applyFilter(Reader reader, Writer writer , Predicate<Issue> filter) throws IOException {
        try (JsonReader jreader = new JsonReader(reader); JsonWriter jwriter = new JsonWriter(writer);) {
            applyFilter(jreader,jwriter,filter);
        }
    }

    public static void applyFilter(JsonReader reader, JsonWriter writer , Predicate<Issue> filter) throws IOException {
        reader.beginArray();
        writer.beginArray();
        while (reader.hasNext()) {
            Issue issue = ISSUE_TYPE_ADAPTER.read(reader);
            if (filter.test(issue)) {
                ISSUE_TYPE_ADAPTER.write(writer,issue);
            }
        }
        writer.endArray();
        reader.endArray();
    }

    /**
     * Aggregate issues read from a file, return a mapping between equivalence classes and class size.
     * @param input
     * @return a mapping between equivalence classes and class sizes
     */
    public static Map<IssueKernel,Integer> readAndAggregateIssues(File input ) throws IOException {
        return readAndAggregateIssues(input,issue -> true);
    }

    public static Map<IssueKernel,Integer> readAndAggregateIssues(File input,Predicate<Issue> filter) throws IOException {
        try (JsonReader reader = new JsonReader(new FileReader(input))) {
            return readAndAggregateIssues(reader,filter);
        }
    }

    static Map<IssueKernel,Integer> readAndAggregateIssues(JsonReader reader) throws IOException {
        return readAndAggregateIssues(reader,issue -> true);
    }

    static Map<IssueKernel,Integer> readAndAggregateIssues(JsonReader reader,Predicate<Issue> filter) throws IOException {
        Map<IssueKernel,Integer> issues = new HashMap<>();
        reader.beginArray();
        while (reader.hasNext()) {
            Issue issue = ISSUE_TYPE_ADAPTER.read(reader);
            if (filter.test(issue)) {
                IssueKernel kernel = issue.getKernel();
                issues.compute(kernel, (k, i) -> i == null ? 1 : i + 1);
            }
        }
        reader.endArray();
        return issues;
    }


    public static List<Issue> readIssues(JsonReader reader) throws IOException {
        return readIssues(reader,issue -> true);
    }

    public static List<Issue> readIssues(JsonReader reader,Predicate<Issue> filter) throws IOException {
        List<Issue> issues = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            Issue issue = ISSUE_TYPE_ADAPTER.read(reader);
            if (filter.test(issue)) {
                issues.add(issue);
            }
        }
        reader.endArray();
        return issues;
    }

    public static List<Issue> readIssues(Reader reader) throws IOException {
        try (JsonReader jreader = new JsonReader(reader)) {
            return readIssues(jreader);
        }
    }

    public static List<Issue> readIssues(Reader reader,Predicate<Issue> filter) throws IOException {
        try (JsonReader jreader = new JsonReader(reader)) {
            return readIssues(jreader,filter);
        }
    }

    public static List<Issue> readIssues(File input) throws IOException {
        try (Reader reader = new FileReader(input)) {
            return readIssues(reader);
        }
    }

    public static List<Issue> readIssues(File input,Predicate<Issue> filter) throws IOException {
        try (Reader reader = new FileReader(input)) {
            return readIssues(reader,filter);
        }
    }

    public static int countIssues (File input) throws IOException {
        return countIssues(input,issue -> true);
    }

    public static int countIssues (File input,Predicate<Issue> filter) throws IOException {
        try (JsonReader reader = new JsonReader(new FileReader(input))) {
            int count = 0;
            reader.beginArray();
            while (reader.hasNext()) {
                Issue issue = ISSUE_TYPE_ADAPTER.read(reader);
                if (filter.test(issue)) {
                    count = count + 1;
                }
            }
            reader.endArray();
            return count;
        }
    }

    public static int countAggregatedIssues (File input) throws IOException {
        return readAndAggregateIssues(input).size();
    }

    public static int countAggregatedIssues (File input,Predicate<Issue> filter) throws IOException {
        return readAndAggregateIssues(input,filter).size();
    }

}
