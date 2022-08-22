package nz.ac.wgtn.nullinference.experiments.descr;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data structure describing a method descriptor.
 * @author jens dietrich
 */
public class MethodDescriptor {
    private String returnType = null;
    private List<String> paramTypes = new ArrayList<>();
    private String rawDescriptor = null; // as in bytecode

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public List<String> getParamTypes() {
        return paramTypes;
    }

    public void addParamType(String paramType) {
        this.paramTypes.add(paramType);
    }

    public String getRawDescriptor() {
        return rawDescriptor;
    }

    public void setRawDescriptor(String rawDescriptor) {
        this.rawDescriptor = rawDescriptor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodDescriptor that = (MethodDescriptor) o;
        return Objects.equals(returnType, that.returnType) &&
                Objects.equals(paramTypes, that.paramTypes) &&
                Objects.equals(rawDescriptor, that.rawDescriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(returnType, paramTypes, rawDescriptor);
    }
}

