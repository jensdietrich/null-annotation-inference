/**
 * Abstract super type for issues and issue kernels.
 * @author jens dietrich
 */
public abstract class AbstractIssue {

    public abstract String getClassName();

    public abstract String getMethodName();

    public abstract String getDescriptor();

    public abstract Issue.IssueType getKind();

    public abstract int getArgsIndex() ;
}
