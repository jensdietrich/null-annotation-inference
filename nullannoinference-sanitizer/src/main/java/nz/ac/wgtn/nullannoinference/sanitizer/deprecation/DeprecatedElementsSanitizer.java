package nz.ac.wgtn.nullannoinference.sanitizer.deprecation;

import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.ProjectType;
import nz.ac.wgtn.nullannoinference.sanitizer.Sanitizer;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Sanitizer to filter out issues in deprecated classes, methods or fields.
 * @author jens dietrich
 */
public class DeprecatedElementsSanitizer implements Sanitizer<Issue>  {

    private List<String> deprecatedElements = null;

    public DeprecatedElementsSanitizer(ProjectType projectType, File projectRootFolder, File deprecatedElementsDump) throws IOException {
        deprecatedElements = ExtractDeprecatedElements.findDeprecatedElements(projectType,projectRootFolder,deprecatedElementsDump);
    }

    @Override
    public boolean test(Issue issue) {
        String issueAsText = issue.getClassName() + "::" + issue.getMethodName() + issue.getDescriptor();
        for (String deprecatedElement:deprecatedElements) {
            if (issueAsText.startsWith(deprecatedElement)) { // stars with matched entire classes marked as deprecated
                return false;
            }
        }
        return true;
    }


}
