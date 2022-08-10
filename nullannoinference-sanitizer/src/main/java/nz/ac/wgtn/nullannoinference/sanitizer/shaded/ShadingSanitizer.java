package nz.ac.wgtn.nullannoinference.sanitizer.shaded;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.sanitizer.LogSystem;
import nz.ac.wgtn.nullannoinference.sanitizer.Sanitizer;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 *  Sanitisies issues in shaded packages.
 *  @author jens dietrich
 */
public class ShadingSanitizer implements Sanitizer<Issue> {
    public static final Logger LOGGER = LogSystem.getLogger("shading-analysis");

    private Set<ShadingSpec> shadingSpecs = null;

    public ShadingSanitizer(File shadingSpecDefs) throws IOException {
        this.shadingSpecs = readShadingSpecs(shadingSpecDefs);
    }

    // for testing
    public ShadingSanitizer(Set<ShadingSpec> shadingSpecs) throws IOException {
        this.shadingSpecs = shadingSpecs;
    }

    @Override
    public boolean test(Issue issue) {
        for (ShadingSpec spec : shadingSpecs) {
            if (issue.getClassName().startsWith(spec.getRenamed())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String name() {
        return "ignore-issues-in-shaded-classes";
    }

    private Set<ShadingSpec> readShadingSpecs(File file) throws IOException {
        Preconditions.checkState(file.exists());
        Gson gson = new Gson();
        try (FileReader in = new FileReader(file)) {
            Type listType = new TypeToken<HashSet<ShadingSpec>>(){}.getType();
            return gson.fromJson(in, listType);
        }
    }
}
