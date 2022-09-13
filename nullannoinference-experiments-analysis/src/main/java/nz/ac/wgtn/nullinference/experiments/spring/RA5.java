package nz.ac.wgtn.nullinference.experiments.spring;

import com.google.common.collect.Sets;
import nz.ac.wgtn.nullannoinference.commons.AbstractIssue;
import nz.ac.wgtn.nullannoinference.commons.IssueKernel;
import nz.ac.wgtn.nullinference.experiments.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static nz.ac.wgtn.nullinference.experiments.spring.Config.*;

/**
 * Script to produce data for RA5.
 * @author jens dietrich
 */
public class RA5 extends Experiment {


    public static final File OUTPUT_CSV = new File("experiments-spring/results/ra/ra5.csv");
    public static final File OUTPUT_LATEX = new File("experiments-spring/results/ra/ra5.tex");

    public static void main (String[] args) throws IOException, InterruptedException {
        new RA5().analyse();
    }

    public void analyse()  {
        Column[] columns = new Column[] {
            Column.First,
            new Column() {
                @Override
                public String name() {
                    return "fields";
                }

                @Override
                public String value(String dataName) {
                    return annotationsToAdd(EXTRACTED_ISSUES_FOLDER,OBSERVED_AND_PROPAGATED_ISSUES_FOLDER,dataName,AGGR_FIELDS_ONLY);
                }
            },
            new Column() {
                @Override
                public String name() {
                    return "params";
                }

                @Override
                public String value(String dataName) {
                    return annotationsToAdd(EXTRACTED_ISSUES_FOLDER,OBSERVED_AND_PROPAGATED_ISSUES_FOLDER,dataName,AGGR_PARAM_ONLY);
                }
            },
            new Column() {
                @Override
                public String name() {
                    return "returns";
                }

                @Override
                public String value(String dataName) {
                    return annotationsToAdd(EXTRACTED_ISSUES_FOLDER,OBSERVED_AND_PROPAGATED_ISSUES_FOLDER,dataName,AGGR_RETURNS_ONLY);
                }
            },
            new Column() {
                @Override
                public String name() {
                    return "all";
                }

                @Override
                public String value(String dataName) {
                    return annotationsToAdd(EXTRACTED_ISSUES_FOLDER,OBSERVED_AND_PROPAGATED_ISSUES_FOLDER,dataName,AGGRE_ALL);
                }
            }

        };

        TableGenerator csvOutput = new CSVTableGenerator(OUTPUT_CSV);
        TableGenerator latexOutput = new LatexTableGenerator(OUTPUT_LATEX,"|lrrrr|");

        this.run(SPRING_MODULES,"RA5 -- annotations to be added by type, value in brackets are classes to be modified","tab:ra5",columns,csvOutput,latexOutput);

    }

    private static String annotationsToAdd(File existing, File inferred, String moduleName, Predicate<? extends AbstractIssue> filter) {
         Set<IssueKernel> existingIssues = (Set<IssueKernel>)readIssues(existing,moduleName,true,filter);
         Set<IssueKernel> inferredIssues = (Set<IssueKernel>)readIssues(inferred,moduleName,true,filter);
         Set<IssueKernel> newIssues = Sets.difference(inferredIssues,existingIssues);
         Set<String> classes = newIssues.stream().map(issue -> issue.getClassName()).collect(Collectors.toSet());
         return Utils.format(newIssues.size()) + " (" + classes.size() + ")";
    }

}
