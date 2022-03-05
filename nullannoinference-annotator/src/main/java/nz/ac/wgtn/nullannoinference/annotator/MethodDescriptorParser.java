package nz.ac.wgtn.nullannoinference.annotator;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Based on japicmp.util.MethodDescriptorParser (thanks !!), with a slight modification to allow parsing of descriptors without return type
 * used in constructors.
 * @author jens dietrich
 */
public class MethodDescriptorParser {
    private final List<String> parameters = new LinkedList();
    private String returnType = "void";

    public MethodDescriptorParser() {
    }

    public void parse(String methodDescriptor) {
        int parenthesisCloseIndex = methodDescriptor.indexOf(41);
        if (parenthesisCloseIndex > -1) {
            this.parseParameters(methodDescriptor, parenthesisCloseIndex);
            this.parseReturnValue(methodDescriptor, parenthesisCloseIndex);
        }

    }

    private void parseReturnValue(String signature, int parenthesisCloseIndex) {
        String retValPart = signature.substring(parenthesisCloseIndex + 1);
        if (retValPart.isEmpty()) {
            this.returnType = null;
        }
        else {
            List<String> retValTypes = this.parseTypes(retValPart);
            this.returnType = (String) retValTypes.get(0);
        }
    }

    private void parseParameters(String signature, int parenthesisCloseIndex) {
        String paramPart = signature.substring(1, parenthesisCloseIndex);
        List<String> paramTypes = this.parseTypes(paramPart);
        this.parameters.clear();
        this.parameters.addAll(paramTypes);
    }

    public List<String> parseTypes(String paramPart) {
        List<String> types = new LinkedList();
        boolean arrayNotation = false;

        for(int i = 0; i < paramPart.length(); ++i) {
            char c = paramPart.charAt(i);
            String type = "void";
            switch(c) {
                case 'B':
                    type = "byte";
                    break;
                case 'C':
                    type = "char";
                    break;
                case 'D':
                    type = "double";
                    break;
                case 'E':
                case 'G':
                case 'H':
                case 'K':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'T':
                case 'U':
                case 'W':
                case 'X':
                case 'Y':
                default:
                    throw new IllegalStateException("Unknown type signature: '" + c + "'");
                case 'F':
                    type = "float";
                    break;
                case 'I':
                    type = "int";
                    break;
                case 'J':
                    type = "long";
                    break;
                case 'L':
                    StringBuilder fqn = new StringBuilder();
                    ++i;

                    for(; i < paramPart.length(); ++i) {
                        c = paramPart.charAt(i);
                        if (c == ';') {
                            break;
                        }

                        if (c == '/') {
                            fqn.append('.');
                        } else {
                            fqn.append(c);
                        }
                    }

                    type = fqn.toString();
                    break;
                case 'S':
                    type = "short";
                    break;
                case 'V':
                    type = "void";
                    break;
                case 'Z':
                    type = "boolean";
                    break;
                case '[':
                    arrayNotation = true;
                    continue;
            }

            if (arrayNotation) {
                type = type + "[]";
                arrayNotation = false;
            }

            types.add(type);
        }

        return types;
    }

    public List<String> getParameters() {
        return this.parameters;
    }

    public String getReturnType() {
        return this.returnType;
    }

    public String getMethodSignature(String methodName) {
        StringBuilder sb = new StringBuilder();
        int counter = 0;

        for(Iterator var4 = this.parameters.iterator(); var4.hasNext(); ++counter) {
            String parameter = (String)var4.next();
            if (counter > 0) {
                sb.append(",");
            }

            sb.append(parameter);
        }

        return methodName + "(" + sb.toString() + ")";
    }
}
