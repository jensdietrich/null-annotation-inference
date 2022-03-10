package nz.ac.wgtn.nullinference.experiments;

import java.util.Objects;

/**
 * Structure mapped to infer json reports for easy deserialization.
 * @author jens dietrich
 */
public class FBInferIssue {

    private String bug_type = null;
    private String severity = null;
    private int line = -1;
    private String file = null;
    private String procedure = null;

    public FBInferIssue(String bug_type, String severity, int line, String file, String procedure) {
        this.bug_type = bug_type;
        this.severity = severity;
        this.line = line;
        this.file = file;
        this.procedure = procedure;
    }

    public FBInferIssue() {
    }

    public String getBug_type() {
        return bug_type;
    }

    public void setBug_type(String bug_type) {
        this.bug_type = bug_type;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getProcedure() {
        return procedure;
    }

    public void setProcedure(String procedure) {
        this.procedure = procedure;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FBInferIssue that = (FBInferIssue) o;
        return line == that.line && Objects.equals(bug_type, that.bug_type) && Objects.equals(severity, that.severity) && Objects.equals(file, that.file) && Objects.equals(procedure, that.procedure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bug_type, severity, line, file, procedure);
    }
}
