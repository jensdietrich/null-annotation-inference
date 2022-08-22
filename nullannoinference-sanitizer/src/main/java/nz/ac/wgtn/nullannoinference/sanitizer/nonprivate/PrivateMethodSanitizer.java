package nz.ac.wgtn.nullannoinference.sanitizer.nonprivate;

import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.ProjectType;
import nz.ac.wgtn.nullannoinference.sanitizer.Sanitizer;
import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Sanitizer to filter out private (incl package-private) methods.
 * @author jens dietrich
 */
public class PrivateMethodSanitizer implements Sanitizer<Issue>  {

    private Set<String> privateElements = null;

    public PrivateMethodSanitizer(ProjectType projectType, File projectRootFolder, File privateMethodsDump) throws IOException {
        privateElements = ExtractPrivateMethods.findPrivateMethods(projectType,projectRootFolder,privateMethodsDump);
    }

    @Override
    public boolean test(Issue issue) {
        String method = issue.getClassName() + "::" + issue.getMethodName() + issue.getDescriptor();
        return !privateElements.contains(method);
    }

    @Override
    public String name() {
        return "ignore-issues-in-private-methods";
    }
}
