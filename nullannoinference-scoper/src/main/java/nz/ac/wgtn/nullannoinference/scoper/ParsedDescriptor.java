package nz.ac.wgtn.nullannoinference.scoper;

import java.util.LinkedList;
import java.util.List;

/**
 * Parses descriptors.
 * @author jens dietrich
 */
public class ParsedDescriptor {

	enum Kind {VOID,OBJECT,ARRAY,PRIMITIVE}

	private final List<Kind> parameters = new LinkedList<>();
	private Kind returnType = Kind.VOID;

	/**
	 * Parses a method descriptor as specified in the Java Virtual Machine Specification (see http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.3.3).
	 * @param methodDescriptor the method descriptor
	 */
	public void parse(String methodDescriptor) {
		int parenthesisCloseIndex = methodDescriptor.indexOf(')');
		if (parenthesisCloseIndex > -1) {
			parseParameters(methodDescriptor, parenthesisCloseIndex);
			parseReturnValue(methodDescriptor, parenthesisCloseIndex);
		}
	}

	private void parseReturnValue(String signature, int parenthesisCloseIndex) {
		String retValPart = signature.substring(parenthesisCloseIndex + 1);
		List<Kind> retValTypes = parseTypes(retValPart);
		returnType = retValTypes.get(0);
	}

	private void parseParameters(String signature, int parenthesisCloseIndex) {
		String paramPart = signature.substring(1, parenthesisCloseIndex);
		List<Kind> paramTypes = parseTypes(paramPart);
		parameters.clear();
		parameters.addAll(paramTypes);
	}

	public List<Kind> parseTypes(String paramPart) {
		List<Kind> types = new LinkedList<>();
		boolean arrayNotation = false;
		for (int i = 0; i < paramPart.length(); i++) {
			char c = paramPart.charAt(i);
			Kind type = Kind.VOID;
			switch (c) {
				case 'Z':
					type = Kind.PRIMITIVE;
					break;
				case 'B':
					type = Kind.PRIMITIVE;
					break;
				case 'C':
					type = Kind.PRIMITIVE;
					break;
				case 'S':
					type = Kind.PRIMITIVE;
					break;
				case 'I':
					type = Kind.PRIMITIVE;
					break;
				case 'J':
					type = Kind.PRIMITIVE;
					break;
				case 'F':
					type = Kind.PRIMITIVE;
					break;
				case 'D':
					type = Kind.PRIMITIVE;
					break;
				case 'V':
					type = Kind.PRIMITIVE;
					break;
				case '[':
					arrayNotation = true;
					continue;
				case 'L':
					StringBuilder fqn = new StringBuilder();
					i++;
					while (i < paramPart.length()) {
						c = paramPart.charAt(i);
						if (c == ';') {
							break;
						} else if (c == '/') {
							fqn.append('.');
						} else {
							fqn.append(c);
						}
						i++;
					}
					type = Kind.OBJECT;
					break;
				default:
					throw new IllegalStateException("Unknown type signature: '" + c + "'");
			}
			if (arrayNotation) {
				type = Kind.ARRAY;
				arrayNotation = false;
			}
			types.add(type);
		}
		return types;
	}

	private int getRefParamCount = 0;

	public int getNumberOfRefParameters() {
		return (int)parameters.stream().filter(p -> p==Kind.ARRAY || p==Kind.OBJECT).count();
	}

	public boolean isRefReturnType() {
		return returnType==Kind.OBJECT || returnType==Kind.ARRAY;
	}

}
