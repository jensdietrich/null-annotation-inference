package nz.ac.wgtn.nullannoinference.annotator;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.Problem;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.base.Preconditions;
import japicmp.util.MethodDescriptorParser;
import nz.ac.wgtn.nullannoinference.commons.Issue;
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

    /**
     *
     * @param originalJavaFile
     * @param transformedJavaFile
     * @param issues
     * @return the number of annotations added
     * @throws IOException
     * @throws AmbiguousAnonymousInnerClassResolutionException
     */
    public int annotateMethod(@Nonnull  File originalJavaFile, @Nonnull File transformedJavaFile, Set<Issue> issues) throws IOException, AmbiguousAnonymousInnerClassResolutionException, JavaParserFailedException {
        Preconditions.checkArgument(originalJavaFile.exists());
        int annotationsAddedCounter = 0;
        ParseResult<CompilationUnit> result = new JavaParser().parse(originalJavaFile);
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
            annotationsAddedCounter = annotationsAddedCounter + annotateMethod(cu, issue.getClassName(), issue.getMethodName(), issue.getDescriptor(), issue.getKind()==Issue.IssueType.RETURN_VALUE?-1:issue.getArgsIndex()) ;
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


    /**
     *
     * @param cu
     * @param typeName
     * @param methodName
     * @param descriptor
     * @param argPosition
     * @return  the number of annotations added
     * @throws IOException
     * @throws AmbiguousAnonymousInnerClassResolutionException
     */
    private int annotateMethod(@Nonnull CompilationUnit cu, @Nonnull String typeName, @Nonnull String methodName, @Nonnull String descriptor, int argPosition) throws IOException, AmbiguousAnonymousInnerClassResolutionException {

        String localTypeName = typeName.contains(".")
                ? typeName.substring(typeName.lastIndexOf(".") + 1, typeName.length())
                : typeName;

        String packageName = typeName.contains(".")
                ? typeName.substring(0,typeName.lastIndexOf(".")) : "";

        Optional<PackageDeclaration> pckDecl = cu.getPackageDeclaration();
        String declaredPackageName = pckDecl.isPresent() ? pckDecl.get().getNameAsString() : "";

        if (!packageName.equals(declaredPackageName)) {
            return 0;
        }

        boolean hasAnonymInnerClass = Stream.of(localTypeName.split("\\$"))
                .anyMatch(name -> NUM_REGEX.matcher(name).matches());

        int annotationAddedCount = 0;
        MethodDeclaration method = null;
        if (hasAnonymInnerClass) {
            method = locateMethodInAnoInnerClass(cu,typeName, methodName, descriptor);
        }
        else {
            method = locateMethod(cu, localTypeName, methodName, descriptor);
        }
        if (method != null) {
            NodeList<Parameter> params = method.getParameters();
            for (int i = 0; i < params.size(); i++) {
                if (argPosition==i) {
                    Parameter param = params.get(i);
                    boolean hasAnnotation = param.getAnnotations().stream()
                            .map(a -> a.getNameAsString())
                            .anyMatch(n -> n.equals(annotationSpec.getNullableAnnotationName()));
                    if (!hasAnnotation) {
                        param.addAnnotation(new MarkerAnnotationExpr(annotationSpec.getNullableAnnotationName()));
                        annotationAddedCount = annotationAddedCount+1;
                    }
                }
            }

            // return type
            if (argPosition==-1) {
                boolean hasAnnotation = method.getAnnotations().stream()
                        .map(a -> a.getNameAsString())
                        .anyMatch(n -> n.equals(annotationSpec.getNullableAnnotationName()));
                if (!hasAnnotation) {
                    method.addAnnotation(new MarkerAnnotationExpr(annotationSpec.getNullableAnnotationName()));
                    annotationAddedCount = annotationAddedCount+1;
                }
            }
        }

        return annotationAddedCount;

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

    static @Nullable MethodDeclaration locateMethod(@Nonnull  TypeDeclaration type, @Nonnull  String className, @Nonnull  String methodName, @Nonnull  String descriptor, @Nonnull  Stack<String> innerClassStack)   {
        Stack<String> innerClassStack2 = new Stack<>();
        innerClassStack2.addAll(innerClassStack);
        innerClassStack2.add(type.getNameAsString());
        String qClassName = innerClassStack2.stream().collect(Collectors.joining("$"));
        if (className.equals(qClassName)) {
            List<MethodDeclaration> methods = type.getMethods();
            for (MethodDeclaration method : methods) {
                if (method.getNameAsString().equals(methodName) && descriptorMatches(method, descriptor)) {
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

    static @Nullable MethodDeclaration locateMethodInAnoInnerClass(@Nonnull  CompilationUnit cu,@Nonnull String typeName,@Nonnull  String methodName,@Nonnull  String descriptor) throws AmbiguousAnonymousInnerClassResolutionException {
        final List<MethodDeclaration> matchingMethods = new ArrayList<>(); // deliberate, to detect duplicates later
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
                        .filter(md -> descriptorMatches(md,descriptor))
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

    static boolean nonAnoTypePartMatches(@Nonnull  MethodDeclaration method, @Nonnull  String typeName)   {
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


    // to be used by tests -- visibility to be used by tests
    static boolean descriptorMatches(@Nonnull  MethodDeclaration method, @Nonnull  String descriptor)   {
        MethodDescriptorParser parser = new MethodDescriptorParser();
        parser.parse(descriptor);

        String returnType = method.getTypeAsString();
        // note that this is not completely accurate as we are not resolving imports in the compilation unit !!
        if (!parser.getReturnType().endsWith(returnType)) {
            return false;
        }

        List<String> paramTypes = method.getParameters().stream().map(p -> p.getTypeAsString()).collect(Collectors.toList());
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
}
