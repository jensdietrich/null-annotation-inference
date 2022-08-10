package nz.ac.wgtn.nullannoinference.sanitizer.shaded;

import java.util.Objects;

/**
 * Data structure to represent shading information (renaming of packages at built time),
 * suitable for data binding with gson, jackson or similar.
 * @author jens dietrich
 */
public class ShadingSpec {
    // the name of the module (optional, for complex multi-module projects)
    private String module = null;
    // the original package name (root)
    private String original = null;
    // the renamed package name (root)
    private String renamed = null;

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public String getRenamed() {
        return renamed;
    }

    public void setRenamed(String renamed) {
        this.renamed = renamed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShadingSpec that = (ShadingSpec) o;
        return Objects.equals(module, that.module) && Objects.equals(original, that.original) && Objects.equals(renamed, that.renamed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(module, original, renamed);
    }
}
