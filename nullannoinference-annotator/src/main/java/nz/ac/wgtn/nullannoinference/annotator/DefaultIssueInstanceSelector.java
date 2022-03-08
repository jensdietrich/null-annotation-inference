package nz.ac.wgtn.nullannoinference.annotator;

import com.google.common.base.Preconditions;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.IssueKernel;
import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Default implementation of a function to select an instance from a class of equivalent issues.
 * @author jens dietrich
 */
public class DefaultIssueInstanceSelector implements IssueInstanceSelector {
    @Override
    public Issue pick(@Nonnull IssueKernel kernel, @Nonnull Set<Issue> instances) {
        Preconditions.checkArgument(instances!=null);
        Preconditions.checkArgument(!instances.isEmpty());
        Issue issue = instances.iterator().next();
        assert issue.getKernel().equals(kernel);
        return issue;
    }
}
