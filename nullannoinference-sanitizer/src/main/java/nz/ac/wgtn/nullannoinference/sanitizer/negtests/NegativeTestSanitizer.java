package nz.ac.wgtn.nullannoinference.sanitizer.negtests;

import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.ProjectType;
import nz.ac.wgtn.nullannoinference.sanitizer.LogSystem;
import nz.ac.wgtn.nullannoinference.sanitizer.Sanitizer;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sanitises negative tests. This includes the extraction of negative tests from the project.
 * @author jens dietrich
 */
public class NegativeTestSanitizer implements Sanitizer<Issue> {

    public static final Logger LOGGER = LogSystem.getLogger("negative-test-analysis");
    private Collection<String> negativeTestMethods = null;

    public NegativeTestSanitizer(ProjectType projectType, File projectRootFolder, File negativeTestDump) throws IOException {
        Set<MethodInfo> negativeTestMethodInfo = IdentifyNegativeTests.findNegativeTests(projectType,projectRootFolder,negativeTestDump);
        negativeTestMethods = negativeTestMethodInfo.stream()
            .map(mi -> mi.getClassName() + "::" + mi.getName() + ":") // in stacktraces this is followed by a line number
            .collect(Collectors.toSet());
    }


    @Override
    public boolean test(Issue issue) {
        if (issue.getStacktrace()==null || issue.getStacktrace().isEmpty()) {
            LOGGER.warn("cannot analyse issue for negative test cause, no stacktrace found, assume that it is not caused by negative test (issue provenance type is " + issue.getProvenanceType() + ")");
            return false;
        }
        else {
            for (String ste : issue.getStacktrace()) {
                for (String m : negativeTestMethods) {
                    if (ste.startsWith(m)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
