package nz.ac.wgtn.nullannoinference.annotator;

import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.IssueKernel;

import java.util.Set;

/**
 * Abstract function to select an instance from a class of equivalent issues.
 * @author jens dietrich
 */
public interface IssueInstanceSelector {

    // pick an instance -- all issues must have the same kernel, the selected instance must be from the set
    Issue pick(IssueKernel kernel, Set<Issue> issues);
}
