package nz.ac.wgtn.nullannoinference.annotator;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Problem;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.google.common.base.Preconditions;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.JSR305NullableAnnotationProvider;
import nz.ac.wgtn.nullannoinference.commons.NullableAnnotationProvider;
import org.apache.commons.io.FileUtils;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nz.ac.wgtn.nullannoinference.annotator.MvnProjectAnnotator.LOGGER;

/**
 * Annotator for a single compilation unit (which may contain several class definitions).
 * @author jens dietrich
 */
public class ClassAnnotator {

    private static Pattern NUM_REGEX = Pattern.compile("[0-9]+");

    public ClassAnnotator(@Nonnull NullableAnnotationProvider annotationSpec) {
        this.annotationSpec = annotationSpec;
    }

    private @Nonnull
    NullableAnnotationProvider annotationSpec = new JSR305NullableAnnotationProvider();

    public NullableAnnotationProvider getAnnotationSpec() {
        return annotationSpec;
    }

    public void setAnnotationSpec(@Nonnull NullableAnnotationProvider annotationSpec) {
        this.annotationSpec = annotationSpec;
    }

    public int annotateMembers(@Nonnull File originalJavaFile, @Nonnull File transformedJavaFile, Set<Issue> issues, List<AnnotationListener> listeners) throws IOException, JavaParserFailedException {
        Preconditions.checkArgument(originalJavaFile.exists());
        int annotationsAddedCounter = 0;

        CombinedTypeSolver typeSolver = new CombinedTypeSolver(new ReflectionTypeSolver(false));

        ParserConfiguration config = new ParserConfiguration()
                .setSymbolResolver(new JavaSymbolSolver(typeSolver))
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_11);

        ParseResult<CompilationUnit> result = new JavaParser(config).parse(originalJavaFile);
        if (!result.isSuccessful()) {
            LOGGER.error("Error parsing " + originalJavaFile);
            for (Problem problem:result.getProblems()) {
                LOGGER.error("\tmessage: " + problem.getMessage());
                LOGGER.error("\t\tlocation: " + (problem.getLocation().isPresent()?problem.getLocation().get():"?"));
            }
            throw new JavaParserFailedException("Error parsing " + originalJavaFile);
        }
        CompilationUnit cu = result.getResult().get();
        for (Issue issue:issues){
            try {
                boolean isConstructor = issue.getMethodName().equals("<init>");
                if (!isConstructor && (issue.getKind()==Issue.IssueType.RETURN_VALUE || issue.getKind()==Issue.IssueType.ARGUMENT)) {
                    annotationsAddedCounter = annotationsAddedCounter + annotateMethod(originalJavaFile, transformedJavaFile, cu, issue, listeners);
                }
                else if (isConstructor && issue.getKind()==Issue.IssueType.ARGUMENT) {
                    annotationsAddedCounter = annotationsAddedCounter + annotateConstructor(originalJavaFile, transformedJavaFile, cu, issue, listeners);
                }
                else if (issue.getKind()==Issue.IssueType.FIELD) {
                    annotationsAddedCounter = annotationsAddedCounter + annotateField(originalJavaFile, transformedJavaFile, cu, issue, listeners);
                }
                else {
                    LOGGER.warn("unknown issue type encountered: " + issue.getKind());
                }
            }
            catch (AmbiguousAnonymousInnerClassResolutionException x) {
                listeners.stream().forEach(l -> l.annotationFailed(originalJavaFile,AmbiguousAnonymousInnerClassResolutionException.class.getName()));
            }
        }
        if (annotationsAddedCounter>0) {

            // add imports
            NodeList<ImportDeclaration> imports = cu.getImports();

            boolean needsNullableAnnotationImport = true;
            for (ImportDeclaration imp:imports) {
                needsNullableAnnotationImport = needsNullableAnnotationImport
                        && !(imp.isAsterisk() && annotationSpec.getNullableAnnotationPackageName().equals(imp.getNameAsString()))
                        && !(!imp.isAsterisk() && annotationSpec.getNullableAnnotationQName().equals(imp.getNameAsString()));
            }
            if (needsNullableAnnotationImport) {
                imports.add(new ImportDeclaration(annotationSpec.getNullableAnnotationQName(),false,false));
            }

            // write result
            FileUtils.write(transformedJavaFile,cu.toString(), Charset.defaultCharset());
        }
        return annotationsAddedCounter;
    }

    static String getLocalTypeName(@Nonnull String typeName) {
        return typeName.contains(".")
            ? typeName.substring(typeName.lastIndexOf(".") + 1, typeName.length())
            : typeName;
    }

    private boolean checkPackageName(@Nonnull CompilationUnit cu, @Nonnull String typeName) {
        String packageName = typeName.contains(".") ? typeName.substring(0,typeName.lastIndexOf(".")) : "";
        Optional<PackageDeclaration> pckDecl = cu.getPackageDeclaration();
        String declaredPackageName = pckDecl.isPresent() ? pckDecl.get().getNameAsString() : "";
        return packageName.equals(declaredPackageName);
    }

    private int annotateField(@Nonnull File originalJavaFile, @Nonnull File transformedJavaFile, @Nonnull CompilationUnit cu, @Nonnull Issue issue, List<AnnotationListener> listeners) throws AmbiguousAnonymousInnerClassResolutionException {

        if (!checkPackageName(cu,issue.getClassName())) {
            return 0;
        }
        String localTypeName = getLocalTypeName(issue.getClassName());

        boolean hasAnonymInnerClass = Stream.of(localTypeName.split("\\$")).anyMatch(name -> NUM_REGEX.matcher(name).matches());
        int annotationAddedCount = 0;
        FieldDeclaration field = null;
        if (hasAnonymInnerClass) {
            field = locateFieldInAnoInnerClass(cu, issue.getClassName(), issue.getMethodName(), issue.getDescriptor());
        }
        else {
            field = locateField(cu, localTypeName, issue.getMethodName(), issue.getDescriptor());
        }

        if (field==null) {
            return 0;
        }

        boolean hasAnnotation = field.getAnnotations().stream()
            .map(a -> a.getNameAsString())
            .anyMatch(n -> n.equals(annotationSpec.getNullableAnnotationName()));

        if (!hasAnnotation) {
            field.addAnnotation(new MarkerAnnotationExpr(annotationSpec.getNullableAnnotationName()));
            annotationAddedCount = annotationAddedCount+1;
            listeners.stream().forEach(l -> l.annotationAdded(originalJavaFile,transformedJavaFile,issue));
        }

        return annotationAddedCount;
    }
    private int annotateConstructor(@Nonnull File originalJavaFile, @Nonnull File transformedJavaFile, @Nonnull CompilationUnit cu, @Nonnull Issue issue, List<AnnotationListener> listeners) throws AmbiguousAnonymousInnerClassResolutionException {

        if (!checkPackageName(cu,issue.getClassName())) {
            return 0;
        }
        String localTypeName = getLocalTypeName(issue.getClassName());

        boolean hasAnonymInnerClass = Stream.of(localTypeName.split("\\$")).anyMatch(name -> NUM_REGEX.matcher(name).matches());
        int annotationAddedCount = 0;
        ConstructorDeclaration constructor = null;
        if (hasAnonymInnerClass) {
            constructor = locateConstructorInAnoInnerClass(cu, issue.getClassName(), issue.getDescriptor());
        }
        else {
            constructor = locateConstructor(cu, localTypeName, issue.getDescriptor());
        }
        if (constructor != null) {
            NodeList<Parameter> params = constructor.getParameters();
            assert issue.getKind()== Issue.IssueType.ARGUMENT;
            for (int i = 0; i < params.size(); i++) {
                if (issue.getArgsIndex() == i) {
                    Parameter param = params.get(i);
                    boolean hasAnnotation = param.getAnnotations().stream()
                        .map(a -> a.getNameAsString())
                        .anyMatch(n -> n.equals(annotationSpec.getNullableAnnotationName()));
                    if (!hasAnnotation) {
                        param.addAnnotation(new MarkerAnnotationExpr(annotationSpec.getNullableAnnotationName()));
                        annotationAddedCount = annotationAddedCount + 1;
                        listeners.stream().forEach(l -> l.annotationAdded(originalJavaFile, transformedJavaFile, issue));
                    }
                }
            }
        }
        return annotationAddedCount;
    }


    private int annotateMethod(@Nonnull File originalJavaFile, @Nonnull File transformedJavaFile, @Nonnull CompilationUnit cu, @Nonnull Issue issue, List<AnnotationListener> listeners) throws AmbiguousAnonymousInnerClassResolutionException {

        if (!checkPackageName(cu,issue.getClassName())) {
            return 0;
        }
        String localTypeName = getLocalTypeName(issue.getClassName());

        boolean hasAnonymInnerClass = Stream.of(localTypeName.split("\\$")).anyMatch(name -> NUM_REGEX.matcher(name).matches());
        int annotationAddedCount = 0;
        MethodDeclaration method = null;
        if (hasAnonymInnerClass) {
            method = locateMethodInAnoInnerClass(cu, issue.getClassName(), issue.getMethodName(), issue.getDescriptor());
        }
        else {
            method = locateMethod(cu, localTypeName, issue.getMethodName(), issue.getDescriptor());
        }
        if (method != null) {
            NodeList<Parameter> params = method.getParameters();
            if (issue.getKind()== Issue.IssueType.ARGUMENT) {
                for (int i = 0; i < params.size(); i++) {
                    if (issue.getArgsIndex() == i) {
                        Parameter param = params.get(i);
                        boolean hasAnnotation = param.getAnnotations().stream()
                                .map(a -> a.getNameAsString())
                                .anyMatch(n -> n.equals(annotationSpec.getNullableAnnotationName()));
                        if (!hasAnnotation) {
                            param.addAnnotation(new MarkerAnnotationExpr(annotationSpec.getNullableAnnotationName()));
                            annotationAddedCount = annotationAddedCount + 1;
                            listeners.stream().forEach(l -> l.annotationAdded(originalJavaFile, transformedJavaFile, issue));
                        }
                    }
                }
            }
            // return type
            if (issue.getKind()== Issue.IssueType.RETURN_VALUE) {
                // assert issue.getArgsIndex()==-1; // might be 0 -- fix upstream but not too important, can just be ignored
                boolean hasAnnotation = method.getAnnotations().stream()
                    .map(a -> a.getNameAsString())
                    .anyMatch(n -> n.equals(annotationSpec.getNullableAnnotationName()));
                if (!hasAnnotation) {
                    method.addAnnotation(new MarkerAnnotationExpr(annotationSpec.getNullableAnnotationName()));
                    annotationAddedCount = annotationAddedCount+1;
                    listeners.stream().forEach(l -> l.annotationAdded(originalJavaFile,transformedJavaFile,issue));
                }
            }
        }
        return annotationAddedCount;
    }

    static @Nullable FieldDeclaration locateField(@Nonnull  CompilationUnit cu,@Nonnull  String className,@Nonnull  String fieldName,@Nonnull  String descriptor)   {
        for (TypeDeclaration type:cu.getTypes()) {
            FieldDeclaration field = locateField(type,className,fieldName,descriptor,new Stack<>());
            if (field!=null) {
                return field;
            }
        }
        return null;
    }

    // visibility to be used by tests
    static @Nullable MethodDeclaration locateMethod(@Nonnull  CompilationUnit cu,@Nonnull  String className,@Nonnull  String methodName,@Nonnull  String descriptor)   {
        for (TypeDeclaration type:cu.getTypes()) {
            MethodDeclaration method = locateMethod(type,className,methodName,descriptor,new Stack<>());
            if (method!=null) {
                return method;
            }
        }
        return null;
    }

    static @Nullable ConstructorDeclaration locateConstructor(@Nonnull  CompilationUnit cu,@Nonnull  String className,@Nonnull  String descriptor)   {
        for (TypeDeclaration type:cu.getTypes()) {
            ConstructorDeclaration constructor = locateConstructor(type,className,descriptor,new Stack<>());
            if (constructor!=null) {
                return constructor;
            }
        }
        return null;
    }

    static @Nullable FieldDeclaration locateField(@Nonnull  TypeDeclaration type, @Nonnull  String className, @Nonnull  String fieldName, @Nonnull  String descriptor, @Nonnull  Stack<String> innerClassStack)   {
        Stack<String> innerClassStack2 = new Stack<>();
        innerClassStack2.addAll(innerClassStack);
        innerClassStack2.add(type.getNameAsString());
        String qClassName = innerClassStack2.stream().collect(Collectors.joining("$"));
        if (className.equals(qClassName)) {
            List<FieldDeclaration> fields = type.getFields();
            Optional<FieldDeclaration> wrappedField = fields.stream()
                .filter(fld -> fld.getVariables().size()==1) // annotations are attached to fields, not individual variables
                .filter(fld -> fld.getVariables().get(0).getNameAsString().equals(fieldName))
                .findFirst();
            return wrappedField.isPresent()?wrappedField.get():null;
        }
        else {
            // check inner classes
            for (Object member:type.getMembers()) {
                if (member instanceof TypeDeclaration) {
                    TypeDeclaration innerClass = (TypeDeclaration)member;
                    FieldDeclaration field = locateField(innerClass,className,fieldName,descriptor,innerClassStack2);
                    if (field!=null) {
                        return field;
                    }
                }
            }
            return null;
        }
    }

    static @Nullable MethodDeclaration locateMethod(@Nonnull  TypeDeclaration type, @Nonnull  String className, @Nonnull  String methodName, @Nonnull  String descriptor, @Nonnull  Stack<String> innerClassStack)   {
        Stack<String> innerClassStack2 = new Stack<>();
        innerClassStack2.addAll(innerClassStack);
        innerClassStack2.add(type.getNameAsString());

        Map<String,String> typeParams = getTypeParameters(type);

        String qClassName = innerClassStack2.stream().collect(Collectors.joining("$"));
        if (className.equals(qClassName)) {
            List<MethodDeclaration> methods = type.getMethods();
            for (MethodDeclaration method : methods) {
                if (method.getNameAsString().equals(methodName) && descriptorMatches(method, descriptor,typeParams)) {
                    return method;
                }
            }
            return null;
        }
        else {
            // check inner classes
            for (Object member:type.getMembers()) {
                if (member instanceof TypeDeclaration) {
                    TypeDeclaration innerClass = (TypeDeclaration)member;
                    MethodDeclaration method = locateMethod(innerClass,className,methodName,descriptor,innerClassStack2);
                    if (method!=null) {
                        return method;
                    }
                }
            }
            return null;
        }
    }

    static @Nullable ConstructorDeclaration locateConstructor(@Nonnull  TypeDeclaration type, @Nonnull  String className, @Nonnull  String descriptor, @Nonnull  Stack<String> innerClassStack)   {
        Stack<String> innerClassStack2 = new Stack<>();
        innerClassStack2.addAll(innerClassStack);
        innerClassStack2.add(type.getNameAsString());

        Map<String,String> typeParams = getTypeParameters(type);

        String qClassName = innerClassStack2.stream().collect(Collectors.joining("$"));
        if (className.equals(qClassName)) {
            List<ConstructorDeclaration> constructors = type.getConstructors();
            for (ConstructorDeclaration constructor : constructors) {
                if (descriptorMatches(constructor, descriptor,typeParams)) {
                    return constructor;
                }
            }
            return null;
        }
        else {
            // check inner classes
            for (Object member:type.getMembers()) {
                if (member instanceof TypeDeclaration) {
                    TypeDeclaration innerClass = (TypeDeclaration)member;
                    ConstructorDeclaration constructor = locateConstructor(innerClass,className,descriptor,innerClassStack2);
                    if (constructor!=null) {
                        return constructor;
                    }
                }
            }
            return null;
        }
    }

    static Map<String,String> getTypeParameters(CompilationUnit cu) {
        if (cu.getTypes().size()>0) {
            return getTypeParameters(cu.getType(0));
        }
        return Collections.EMPTY_MAP;
    }

    static Map<String,String> getTypeParameters(TypeDeclaration type) {
        Map<String,String> typeParams = new HashMap<>();
        if (type instanceof ClassOrInterfaceDeclaration) {
            ClassOrInterfaceDeclaration clType = (ClassOrInterfaceDeclaration)type;
            clType.getTypeParameters().stream().forEach(
                arg -> {
                    String bound = "java.lang.Object";
                    if (arg.getTypeBound().size()>0) {
                       bound = arg.getTypeBound().get(0).getNameAsString();
                    }
                    typeParams.put(arg.getNameAsString(),bound); // TODO type bounds
                }
            );
        }
        return typeParams;
    }

    static @Nullable ConstructorDeclaration locateConstructorInAnoInnerClass(@Nonnull  CompilationUnit cu,@Nonnull  String typeName,@Nonnull  String descriptor) throws AmbiguousAnonymousInnerClassResolutionException {
        final List<ConstructorDeclaration> matchingMethods = new ArrayList<>(); // deliberate, to detect duplicates later
        Map<String,String> typeParameters = getTypeParameters(cu);  // TODO inner classes might have type parameters too
        cu.accept(new VoidVisitorAdapter<Object>() {
            @Override
            public void visit(ObjectCreationExpr n, Object arg) {
                super.visit(n, arg);
                Optional<NodeList<BodyDeclaration<?>>> optClassBody = n.getAnonymousClassBody();
                if (optClassBody.isPresent()) {
                    List<ConstructorDeclaration> matchingConstructors2 = optClassBody.get().stream()
                        .filter(bd -> bd.isConstructorDeclaration())
                        .map(bd -> bd.asConstructorDeclaration())
                        .filter(md -> descriptorMatches(md,descriptor,typeParameters))
                        .filter(md -> nonAnoTypePartMatches(md,typeName))
                        .collect(Collectors.toList());
                    matchingMethods.addAll(matchingConstructors2);
                }
            }
        },null);

        if (matchingMethods.size()==0) {
            return null;
        }
        else if (matchingMethods.size()==1) {
            return matchingMethods.get(0);
        }
        else {
            String qMethodName = typeName + "::<init>"  + descriptor;
            throw new AmbiguousAnonymousInnerClassResolutionException("Ambiguous resolution on anonymous inner class for method: " + qMethodName);
        }
    }

    static @Nullable MethodDeclaration locateMethodInAnoInnerClass(@Nonnull  CompilationUnit cu,@Nonnull String typeName,@Nonnull  String methodName,@Nonnull  String descriptor) throws AmbiguousAnonymousInnerClassResolutionException {
        final List<MethodDeclaration> matchingMethods = new ArrayList<>(); // deliberate, to detect duplicates later
        Map<String,String> typeParameters = getTypeParameters(cu);  // TODO inner classes might have type parameters too
        cu.accept(new VoidVisitorAdapter<Object>() {
            @Override
            public void visit(ObjectCreationExpr n, Object arg) {
                super.visit(n, arg);
                Optional<NodeList<BodyDeclaration<?>>> optClassBody = n.getAnonymousClassBody();
                if (optClassBody.isPresent()) {
                    List<MethodDeclaration> matchingMethods2 = optClassBody.get().stream()
                        .filter(bd -> bd.isMethodDeclaration())
                        .map(bd -> bd.asMethodDeclaration())
                        .filter(md -> md.getNameAsString().equals(methodName))
                        .filter(md -> descriptorMatches(md,descriptor,typeParameters))
                        .filter(md -> nonAnoTypePartMatches(md,typeName))
                        .collect(Collectors.toList());
                    matchingMethods.addAll(matchingMethods2);
                }
            }
        },null);

        if (matchingMethods.size()==0) {
            return null;
        }
        else if (matchingMethods.size()==1) {
            return matchingMethods.get(0);
        }
        else {
            String qMethodName = typeName + "::" + methodName + descriptor;
            throw new AmbiguousAnonymousInnerClassResolutionException("Ambiguous resolution on anonymous inner class for method: " + qMethodName);
        }
    }

    static @Nullable FieldDeclaration locateFieldInAnoInnerClass(@Nonnull  CompilationUnit cu, @Nonnull String typeName, @Nonnull  String fieldName, @Nonnull  String descriptor) throws AmbiguousAnonymousInnerClassResolutionException {
        final List<FieldDeclaration> matchingFields = new ArrayList<>(); // deliberate, to detect duplicates later
        cu.accept(new VoidVisitorAdapter<Object>() {
            @Override
            public void visit(ObjectCreationExpr n, Object arg) {
                super.visit(n, arg);
                Optional<NodeList<BodyDeclaration<?>>> optClassBody = n.getAnonymousClassBody();
                if (optClassBody.isPresent()) {
                    List<FieldDeclaration> matchingFields = optClassBody.get().stream()
                        .filter(bd -> bd.isFieldDeclaration())
                        .map(bd -> bd.asFieldDeclaration())
                        .filter(fld -> fld.getVariables().size()==1) // annotations only to be attached single variable
                        .filter(fld -> fld.getVariables().get(0).getNameAsString().equals(fieldName))
                        .filter(fld -> typeMatches(fld.getVariables().get(0).getType(),descriptor))
                        .filter(fld -> nonAnoTypePartMatches(fld,typeName))
                        .collect(Collectors.toList());
                }
            }
        },null);

        if (matchingFields.size()==0) {
            return null;
        }
        else if (matchingFields.size()==1) {
            return matchingFields.get(0);
        }
        else {
            String qMethodName = typeName + "::" + fieldName + descriptor;
            throw new AmbiguousAnonymousInnerClassResolutionException("Ambiguous resolution on anonymous inner class for method: " + qMethodName);
        }
    }

    private static boolean typeMatches(Type type, String descriptor) {
        System.out.println("TODO: ClassAnnotator::typeMatches");
        return false;
    }


    static boolean nonAnoTypePartMatches(@Nonnull  Node method, @Nonnull  String typeName)   {
        List<String> types = new ArrayList<>();
        collectContext(types,method);
        String prefix = types.stream().collect(Collectors.joining("$"));

        List<String> tokens = new ArrayList<>();
        StringTokenizer tok = new StringTokenizer(typeName,"$");
        while (tok.hasMoreTokens()) {
            String token = tok.nextToken();
            // assume that we wont encounter named classes within anonymous classes
            if (NUM_REGEX.matcher(token).matches()) break;
            else {
                tokens.add(token);
            }
        }
        String typeNameWithoutAnoParts = tokens.stream().collect(Collectors.joining("$"));
        return typeNameWithoutAnoParts.equals(prefix);
    }

    // build type context starting with the outer type
    private static void collectContext(List<String> types, @Nonnull Node node) {

        if (node instanceof CompilationUnit) {
            return;
        }

        if (node instanceof TypeDeclaration) {
            String name = ((TypeDeclaration)node).getNameAsString();
            types.add(0,name);
        }

        Optional<Node> parent = node.getParentNode();
        assert parent.isPresent();
        collectContext(types, parent.get());


    }

    static boolean descriptorMatches(@Nonnull  MethodDeclaration method, @Nonnull  String descriptor,Map<String,String> resolvedTypeParameters) {
        NodeList<TypeParameter> typeParameters = method.getTypeParameters();
        NodeList<Parameter> parameters = method.getParameters();
        Type memberType = method.getType();
        return descriptorMatches(typeParameters,parameters,memberType,descriptor,resolvedTypeParameters);
    }

    static boolean descriptorMatches(@Nonnull  ConstructorDeclaration constructor, @Nonnull  String descriptor,Map<String,String> resolvedTypeParameters) {
        NodeList<TypeParameter> typeParameters = constructor.getTypeParameters();
        NodeList<Parameter> parameters = constructor.getParameters();
        return descriptorMatches(typeParameters, parameters, null, descriptor, resolvedTypeParameters);
    }

    // match between bytecode
    static boolean descriptorMatches(@Nonnull  NodeList<TypeParameter> typeParameters, @Nonnull NodeList<Parameter> parameters,Type memberType, @Nonnull  String descriptor,Map<String,String> resolvedTypeParameters)   {
        MethodDescriptorParser parser = new MethodDescriptorParser();
        parser.parse(descriptor);

        // add type parameters defined in method
        Map<String,String> resolvedTypeParameters2 = new HashMap<>();
        resolvedTypeParameters2.putAll(resolvedTypeParameters);

        // add method type parameters
        for (int i=0;i<typeParameters.size();i++) {
            TypeParameter param = typeParameters.get(i);
            String bond = "java.lang.Object";
            if (param.getTypeBound().size()>0) {
                bond = getRawName(param.getTypeBound().get(0),resolvedTypeParameters2);
            }
            String name = param.getName().asString();
            resolvedTypeParameters2.put(name,bond);
        }

        if (memberType!=null) {
            // method, otherwise this is a constructor
            String returnType = getRawName(memberType, resolvedTypeParameters2);
            String mapped = resolvedTypeParameters2.get(returnType);
            returnType = mapped == null ? returnType : mapped;
            // note that this is not completely accurate as we are not resolving imports in the compilation unit !!
            if (!parser.getReturnType().endsWith(returnType)) {
                return false;
            }
        }

        List<String> paramTypes = parameters.stream()
            .map(p -> getRawName(p.getType(),resolvedTypeParameters2))
            .collect(Collectors.toList());

        // vararg are different in source code, but just arrays in bytecode -- must account for this
        for (int i=0;i<parameters.size();i++) {
            if (parameters.get(i).isVarArgs()) {
                paramTypes.set(i,paramTypes.get(i)+"[]");
            }
        }

        if (paramTypes.size()!=parser.getParameters().size()) {
            return false;
        }
        for (int i=0;i<paramTypes.size();i++) {
            // note that this is not completely accurate as we are not resolving imports in the compilation unit !!
            if (!parser.getParameters().get(i).endsWith(paramTypes.get(i))) {
                return false;
            }
        }

        return true;
    }

    static String getRawName (Type type,Map<String,String> resolvedVariables) {

        if (type instanceof ArrayType) {
            ArrayType arrType = (ArrayType) type;
            return getRawName(arrType.getComponentType(),resolvedVariables) + "[]";
        }
        else if (type instanceof ClassOrInterfaceType) {
            ClassOrInterfaceType clType = (ClassOrInterfaceType)type;
            String name = clType.getNameAsString(); // should exclude type params
            String resolved = resolvedVariables.get(name);
            return resolved==null?name:resolved;
        }
        return type.asString();
    }

}
