package nz.ac.wgtn.nullinference.experiments.spring;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * CSV table generator.
 * @author jens dietrich
 */
public class CSVTableGenerator extends TableGenerator {

    private String sep = "\t";
    private List<List<String>> cells = new ArrayList<>();

    public CSVTableGenerator(File output,String sep) {
        super(output);
        this.sep = sep;
    }
    public CSVTableGenerator(File output) {
        super(output);
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
            String headerLine = Stream.of(this.getColumnNames()).collect(Collectors.joining(sep));
            out.println(headerLine);
            for (List<String> row:cells) {
                String nextLine = row.stream().collect(Collectors.joining(sep));
                out.println(nextLine);
            }
        }
        LOGGER.info("experiment results written in CSV format to " + getOutput().getAbsolutePath());
    }
}
