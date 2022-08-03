package nz.ac.wgtn.nullinference.experiments.spring;

import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Abstract table generator.
 * @author jens dietrich
 */
public abstract class TableGenerator {

    public static final Logger LOGGER = LogSystem.getLogger("experiments");

    private String[] columnNames = null;
    private String caption = null;
    private String label = null;
    private File output = null;

    private NumberFormat intFormat = new DecimalFormat("###,###,###");
    private DecimalFormat decFormat = new DecimalFormat("0.00");

    public TableGenerator(File output) {
        this.output = output;
    }

    public File getOutput() {
        return output;
    }

    public void setOutput(File output) {
        this.output = output;
    }

    public NumberFormat getIntFormat() {
        return intFormat;
    }

    public void setIntFormat(NumberFormat intFormat) {
        this.intFormat = intFormat;
    }

    public DecimalFormat getDecFormat() {
        return decFormat;
    }

    public void setDecFormat(DecimalFormat decFormat) {
        this.decFormat = decFormat;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public abstract void cell(String value);
    public void cell(int value) {
        cell(intFormat.format(value));
    }
    public void cell(double value) {
        cell(decFormat.format(value));
    }
    public abstract void nl();

    public abstract void write() throws IOException;

}
