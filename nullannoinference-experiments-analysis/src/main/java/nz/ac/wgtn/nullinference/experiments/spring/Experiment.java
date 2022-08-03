package nz.ac.wgtn.nullinference.experiments.spring;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import nz.ac.wgtn.nullannoinference.commons.*;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Abstract experiment.
 * @author jens dietrich
 */
public abstract class Experiment {

    public static final Logger LOGGER = LogSystem.getLogger("experiments");

    private static Map<File,Set<Issue>> ISSUE_CACHE = new HashMap<>();
    private static Map<File,Set<ShadingSpec>> SHADING_SPEC_CACHE = new HashMap<>();

    public boolean aggregateIssues () {
        return true;
    }

    protected static int countIssues(File folder, String moduleName, boolean aggregate)  {
        return readIssues(folder,moduleName,aggregate).size();
    }

    protected static int countIssues(File folder, String moduleName, boolean aggregate, Predicate<? extends AbstractIssue> filter)  {
        return readIssues(folder,moduleName,aggregate,filter).size();
    }

    protected static Set<? extends AbstractIssue> readIssues(File folder, String moduleName, boolean aggregate)  {
        return readIssues(folder,moduleName,aggregate, issue -> true);
    }

    protected static Set<? extends AbstractIssue> readIssues(File folder, String moduleName, boolean aggregate, Predicate<? extends AbstractIssue> filter)  {
        File file = new File(folder,"nullable-"+moduleName+".json");
        Preconditions.checkState(file.exists());
        Set<Issue> issues = doReadIssues(file);
        if (aggregate) {
            Set<IssueKernel> aggregatedIssues = IssueAggregator.aggregate(issues);
            LOGGER.info("issues aggregated to " + aggregatedIssues.size());
            aggregatedIssues = aggregatedIssues.stream().filter((Predicate<? super IssueKernel>) filter).collect(Collectors.toSet());
            LOGGER.info("issues filtered: " + aggregatedIssues.size());
            return aggregatedIssues;
        }
        else {
            issues = issues.stream().filter((Predicate<? super Issue>) filter).collect(Collectors.toSet());
            LOGGER.info("issues filtered: " + issues.size());
            return issues;
        }
    }

    private static Set<Issue> doReadIssues(File file) {
        Set<Issue> issues = ISSUE_CACHE.get(file);
        if (issues==null) {
            Gson gson = new Gson();
            try (FileReader in = new FileReader(file)) {
                Type listType = new TypeToken<HashSet<Issue>>() {}.getType();
                issues = gson.fromJson(in, listType);
                ISSUE_CACHE.put(file,issues);
                LOGGER.info(""+issues.size()+" issues read from " + file.getAbsolutePath() );
            } catch (IOException x) {
                LOGGER.error("error reading file " + file.getAbsolutePath(),"x");
                throw new IllegalStateException(x);
            }
        }
        else {
            LOGGER.info("using cached issues read from: " + file.getAbsolutePath());
        }
        return issues;
    }


    protected static Set<ShadingSpec> readShadingSpecs(File file)  {
        Set<ShadingSpec> specs = SHADING_SPEC_CACHE.get(file);
        if (specs==null) {
            Preconditions.checkState(file.exists());
            Gson gson = new Gson();
            try (FileReader in = new FileReader(file)) {
                Type listType = new TypeToken<HashSet<ShadingSpec>>() {}.getType();
                specs = gson.fromJson(in, listType);
                LOGGER.info("" + specs.size() + " shading specs read from " + file.getAbsolutePath());
                SHADING_SPEC_CACHE.put(file,specs);
            } catch (IOException x) {
                LOGGER.error("error reading file " + file.getAbsolutePath(), "x");
                throw new IllegalStateException(x);
            }
        }
        else {
            LOGGER.info("using cached shading specs read from: " + file.getAbsolutePath());
        }
        return specs;

    }

    protected static double jaccardSimilarity(File folder1, File folder2,String moduleName,Predicate<? extends AbstractIssue> filter)  {
        // always work with aggregated sets !
        Set<? extends AbstractIssue> set1 = readIssues(folder1,moduleName,true,filter);
        Set<? extends AbstractIssue> set2 = readIssues(folder2,moduleName,true,filter);
        return ((double)Sets.intersection(set1,set2).size()) / ((double)Sets.union(set1,set2).size());
    }

    protected static String diffMetrics(File folder1, File folder2,String moduleName)  {
        return diffMetrics(folder1,folder2,moduleName,issue -> true);
    }

    protected static String diffMetrics(File folder1, File folder2,String moduleName,Predicate<? extends AbstractIssue> filter)  {
        // always work with aggregated sets !
        Set<? extends AbstractIssue> set1 = readIssues(folder1,moduleName,true,filter);
        Set<? extends AbstractIssue> set2 = readIssues(folder2,moduleName,true,filter);
        double symDiff =  ((double)Sets.intersection(set1,set2).size()) / ((double)Sets.union(set1,set2).size());
        double diffLeftMRight =  ((double)Sets.difference(set1,set2).size()) / ((double)Sets.union(set1,set2).size());
        double diffRightMLeft =  ((double)Sets.difference(set2,set1).size()) / ((double)Sets.union(set1,set2).size());

        return Utils.format(symDiff) + " (" + Utils.format(diffLeftMRight) + "," + Utils.format(diffRightMLeft) + ")" ;
    }

    protected static double jaccardSimilarity(File folder1, File folder2,String moduleName)  {
        return jaccardSimilarity(folder1,folder2,moduleName,issue -> true);
    }

    protected void run(List<String> dataset, String caption, String label, Column[] columns, TableGenerator... output) {
        Preconditions.checkArgument(output.length>0,"at least one table generator is required");
        // header line, meta data
        for (TableGenerator out:output) {
            out.setCaption(caption);
            out.setLabel(label);
            String[] columnNames = new String[columns.length];
            for (int i=0;i<columnNames.length;i++) {
                columnNames[i] = columns[i].name();
            }
            out.setColumnNames(columnNames);

        }

        // body
        for (String data:dataset) {
            for (Column column:columns) {
                String value = column.value(data);
                for (TableGenerator out:output) {
                    out.cell(value);
                }
            }
            for (TableGenerator out:output) {
                out.nl();
            }
            String nextLine = Stream.of(columns).map(col -> col.value(data)).collect(Collectors.joining(" , "));
            System.out.println(nextLine);
        }

        for (TableGenerator out:output) {
            try {
                out.write();
            } catch (IOException x) {
                LOGGER.error("error writing file " + out.getOutput().getAbsolutePath(),x );
            }
        }

    }
}
