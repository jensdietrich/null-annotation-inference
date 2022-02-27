package nz.ac.wgtn.nullannoinference.refiner.lsp;

import java.util.Objects;

/**
 * Simple representation of a Java method. By design it EXCLUDES ownership.
 * @see OwnedMethod
 * @author jens diietrich
 */
public class Method implements Comparable<Method> {
    private String name = null;
    private String descriptor = null;

    public Method(String name, String descriptor) {
        this.name = name;
        this.descriptor = descriptor;
    }

    @Override
    public int compareTo(Method o) {
        return this.toString().compareTo(o.toString());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Method method = (Method) o;
        return Objects.equals(name, method.name) && Objects.equals(descriptor, method.descriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, descriptor);
    }

    @Override
    public String toString() {
        return name + descriptor;
    }
}
