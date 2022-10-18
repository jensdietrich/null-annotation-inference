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
 * Script to produce data for RA6.
 * @author jens dietrich
 */
public class RA6 extends Experiment {

    public static final File OUTPUT_CSV = new File("experiments-spring/results/ra/ra6.csv");
    public static final File OUTPUT_LATEX = new File("experiments-spring/results/ra/ra6.tex");

    public static void main (String[] args) throws IOException, InterruptedException {
        new RA6().analyse();
    }

    public void analyse()  {
        Column[] columns = new Column[]{
            Column.First,
            new Column() {
                @Override
                public String name() {
                    return "original";
                }

                @Override
                public String value(String dataName) {
                    return Utils.format(countIssues(EXTRACTED_ISSUES_FOLDER, dataName, true));
                }
            },
            new Column() {
                @Override
                public String name() {
                    return "re-annotated";
                }

                @Override
                public String value(String dataName) {
                    return Utils.format(countIssues(EXTRACTED_FROM_REANNOTATED_ISSUES_FOLDER, dataName, true));
                }
            },
            new Column() {
                @Override
                public String name() {
                    return "r,p";
                }

                @Override
                public String value(String dataName) {
                    return recallPrecision(EXTRACTED_ISSUES_FOLDER,EXTRACTED_FROM_REANNOTATED_ISSUES_FOLDER, dataName);
                }
            }
        };

        TableGenerator csvOutput = new CSVTableGenerator(OUTPUT_CSV);
        TableGenerator latexOutput = new LatexTableGenerator(OUTPUT_LATEX,"|lrrr|");

        this.run(SPRING_MODULES,"RA6 -- nullable annotation extracted from original projects, re-annotated projects, and recall / precision of automatically inserted annotations with respect to original annotations","tab:ra6",columns,csvOutput,latexOutput);
    }

}
