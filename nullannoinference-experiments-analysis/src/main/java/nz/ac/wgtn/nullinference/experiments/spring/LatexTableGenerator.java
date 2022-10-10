package nz.ac.wgtn.nullinference.experiments.spring;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Latex table generator.
 * @author jens dietrich
 */
public class LatexTableGenerator extends TableGenerator {


    private static class MultiColumnCell {
        int cellWidth = 1;
        String label = null;
        String align = "l";

        private MultiColumnCell(int cellWidth, String label,String align) {
            this.cellWidth = cellWidth;
            this.label = label;
            this.align = align;
        }

        private String toLatex() {
            // \multicolumn{2}{c}{Multi-column}
            return "\\multicolumn{" + cellWidth + "}{" + align + "}{" + label + "}";
        }
    }


    public LatexTableGenerator addMultiColumn(int width,String label,String align) {
        Preconditions.checkArgument(width>0);
        if (multiColumnCells==null) {
            multiColumnCells = new ArrayList<>();
        }
        multiColumnCells.add(new MultiColumnCell(width,label,align));
        return this ; // to use builder - style
    }

    private String alignmentSpec = null;

    private List<MultiColumnCell> multiColumnCells = null;

    private List<List<String>> cells = new ArrayList<>();

    public LatexTableGenerator(File output, String alignmentSpec) {
        super(output);
        this.alignmentSpec = alignmentSpec;
    }

    @Override
    public void cell(String value) {
        if (cells.isEmpty()) {
            cells.add(new ArrayList<>());
        }
        cells.get(cells.size()-1).add(value);
    }

    @Override
    public void nl() {
        cells.add(new ArrayList<>());
    }

    @Override
    public void write() throws IOException {
        try (PrintStream out = new PrintStream(new FileOutputStream(getOutput()))) {

            out.println("\\begin{table}");
            out.println("\\begin{tabular}{" +alignmentSpec + "}");

            out.println("\\hline");
            if (this.multiColumnCells!=null) {
                String multiLine = multiColumnCells.stream()
                        .map(mcc -> mcc.toLatex())
                        .collect(Collectors.joining(" & ",""," \\\\"));
                out.println(multiLine);
            }

            String headerLine = Stream.of(this.getColumnNames()).collect(Collectors.joining(" & ",""," \\\\"));
            out.println(headerLine);
            out.println("\\hline");

            cells.stream().filter(row -> !row.isEmpty()).forEach(row -> {
                String nextLine = row.stream().collect(Collectors.joining(" & ",""," \\\\"));
                out.println(nextLine);
            });
            out.println("\\hline");
            out.println("\\end{tabular}");
            out.println("\\caption{\\label{" + this.getLabel() + "}"+this.getCaption()+"}");
            out.println("\\end{table}");
        }
        LOGGER.info("experiment results written in Latex format to " + getOutput().getAbsolutePath());
    }
}
