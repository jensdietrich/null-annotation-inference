package descr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Simple hand-crafted parser for Java descriptors as defined in the JVM Spec.
 * @author jens dietrich
 */
public abstract class DescriptorParser  {

    public static boolean isPrimitive(String type) {
        return type.equals("boolean")
            || type.equals("char")
            || type.equals("byte")
            || type.equals("short")
            || type.equals("float")
            || type.equals("long")
            || type.equals("double")
            || type.equals("void");
    }

    public static boolean isNullable(String type) {
        return !isPrimitive(type);
    }

    public static MethodDescriptor parseMethodDescriptor (String descr) {
        MethodDescriptor descriptor = new MethodDescriptor();
        descriptor.setRawDescriptor(descr);
        List<Character> chars = new ArrayList<>(descr.length());
        for (char c:descr.toCharArray()) {
            chars.add(c);
        }
        Iterator<Character> iter = chars.listIterator();

        if (!iter.hasNext()) throw new IllegalArgumentException("Descriptor must not be empty");
        if (iter.next()!='(') throw new IllegalArgumentException("Descriptor must start with (");

        String nextType = null;
        while ((nextType = parseType(iter))!=null) {
            descriptor.addParamType(nextType);
        }
        if (iter.hasNext()) {
            // constructors (should have V, could be trimmed)
            nextType = parseType(iter);
            if (nextType!=null) {
                descriptor.setReturnType(nextType);
            }
        }

        return descriptor;
    }

    public static String parseFieldDescriptor (String descr)  {
        List<Character> chars = new ArrayList<>(descr.length());
        for (char c:descr.toCharArray()) {
            chars.add(c);
        }
        Iterator<Character> iter = chars.iterator();
        String d = parseType(iter);
        if (d==null) throw new IllegalArgumentException("This is not a valid field descriptor: " + descr);
        if (d.equals("void")) throw new IllegalArgumentException("This is not a valid field descriptor: " + descr);
        if (iter.hasNext()) throw new IllegalArgumentException("This is not a valid field descriptor: " + descr);
        return d;
    }

    public static String parseType (String descr)  {
        return descr.replace('/','.');
    }

    private static String parseType(Iterator<Character> iter) {
        char c = iter.next();
        if (c=='I') return "int";
        else if (c=='Z') return "boolean";
        else if (c=='C') return "char";
        else if (c=='B') return "byte";
        else if (c=='S') return "short";
        else if (c=='F') return "float";
        else if (c=='J') return "long";
        else if (c=='D') return "double";
        else if (c=='[') return parseArrayType(iter);
        else if (c=='L') return parseRefType(iter);
        else if (c==')') return null;
        else if (c=='V') return "void";

        // this should not be possible
        else throw new IllegalArgumentException();
    }

    private static String parseRefType( Iterator<Character> iter) {
        StringBuffer b = new StringBuffer();
        while (iter.hasNext()) {
            char c = iter.next();
            if (c==';') {
                // done
                String name = b.toString();
                return name;
            }
            if (c=='/') {
                c = '.';
            }
            b.append(c);
        }
        throw new IllegalArgumentException();
    }

    private static String parseArrayType(Iterator<Character> iter) {
        if (!iter.hasNext()) throw new IllegalArgumentException();
        String componentType = parseType(iter);
        return componentType + "[]";
    }

}
