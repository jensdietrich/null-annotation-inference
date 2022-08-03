package nz.ac.wgtn.nullinference.experiments.spring;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import nz.ac.wgtn.nullannoinference.commons.AbstractIssue;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.IssueAggregator;
import nz.ac.wgtn.nullannoinference.commons.IssueKernel;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Abstract experiment.
 * @author jens dietrich
 */
public abstract class Experiment {

    public static final Logger LOGGER = LogSystem.getLogger("experiments");

    public boolean aggregateIssues () {
        return true;
    }

    protected static int countIssues(File folder, String moduleName, boolean aggregate)  {
        return readIssues(folder,moduleName,aggregate).size();
    }

    protected static Set<? extends AbstractIssue> readIssues(File folder, String moduleName, boolean aggregate)  {
        File file = new File(folder,"nullable-"+moduleName+".json");
        Preconditions.checkState(file.exists());
        Gson gson = new Gson();
        try (FileReader in = new FileReader(file)) {
            Type listType = new TypeToken<HashSet<Issue>>() {}.getType();
            Set<Issue> issues = gson.fromJson(in, listType);
            LOGGER.info(""+issues.size()+" issues read from " + file.getAbsolutePath() );

            if (aggregate) {
                Set<IssueKernel> aggregatedIssues = IssueAggregator.aggregate(issues);
                LOGGER.info("issues aggregated to " + aggregatedIssues.size());
                return aggregatedIssues;
            }
            else {
                return issues;
            }
        } catch (IOException x) {
            RA1.LOGGER.error("error reading file " + file.getAbsolutePath(),"x");
            throw new IllegalStateException(x);
        }
    }

    protected static double jaccardSimilarity(File folder1, File folder2,String moduleName)  {
        // always work with aggregated sets !
        Set<? extends AbstractIssue> set1 = readIssues(folder1,moduleName,true);
        Set<? extends AbstractIssue> set2 = readIssues(folder2,moduleName,true);
        return ((double)Sets.intersection(set1,set2).size()) / ((double)Sets.union(set1,set2).size());
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
