package nz.ac.wgtn.nullannoinference.agent;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.asm.MemberSubstitution;
import net.bytebuddy.description.ModifierReviewable;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.bytebuddy.description.type.TypeDescription.VOID;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Null checks.
 * @author jens dietrich
 */
public abstract class NullChecks {

    public static final String CONTEXT = System.getProperty("semdiff-nullable-analyser-context");

    static {
        System.out.println(NullChecks.class.getName() + "::CONTEXT set to " + CONTEXT);
    }

    // reusable checks accessed by instrumented code
     public static void checkArguments(Executable method, Object[] args){
         if (mustCheck(method)) {
             for (int i = 0; i < args.length; i++) {
                 if (args[i] == null) {
                     String name = method instanceof Constructor ? "<init>" : method.getName();
                     String descriptor = getDescriptor(method);
                     Issue issue = new Issue(method.getDeclaringClass().getName(), name, descriptor, CONTEXT, Issue.IssueType.ARGUMENT, i);
                     IssueStore.add(issue);
                 }
             }
         }
    }

    public static void checkReturn(Executable method, Object val){
        if (mustCheck(method) && (method instanceof Method) && ((Method)method).getReturnType()!=Void.TYPE && val==null) {
            String descriptor = getDescriptor(method);
            Issue issue = new Issue(method.getDeclaringClass().getName(), method.getName(), descriptor, CONTEXT, Issue.IssueType.RETURN_VALUE, -1);
            IssueStore.add(issue);
        }
    }

    public static boolean mustCheck(Executable method) {
         return !method.isSynthetic();
    }

    private static String getDescriptor(Executable method) {
         String s =  Stream.of(method.getParameterTypes())
             .map(cl -> getTypeNameInByteCodeFormat(cl))
             .collect(Collectors.joining("","(",")"));
         if (method instanceof Method) {
             s = s + getTypeNameInByteCodeFormat(((Method)method).getReturnType());
         }
         return s;
    }


    static String getTypeNameInByteCodeFormat (Class type) {

         if (type.isArray()) {
             return "[" + getTypeNameInByteCodeFormat(type.getComponentType());
         }

         if (type==Byte.TYPE) {
            return "B";
         }
         else if (type==Character.TYPE) {
            return "C";
         }
         else if (type==Double.TYPE) {
             return "D";
         }
         else if (type==Float.TYPE) {
             return "F";
         }
         else if (type==Integer.TYPE) {
             return "I";
         }
         else if (type==Long.TYPE) {
             return "J";
         }
         else if (type==Short.TYPE) {
             return "S";
         }
         else if (type==Boolean.TYPE) {
             return "Z";
         }
         else if (type==Void.TYPE) {
             return "V";
         }
         else {
             return "L" + type.getName().replace('.','/') + ";";
         }
    }

    public static class MethodWithoutReturnValues {

        public static final AsmVisitorWrapper VISITOR = Advice.to(MethodWithoutReturnValues.class)
            .on(any()
            .and(not(isTypeInitializer()))
            .and(returns(VOID))
            .and(not(ModifierReviewable.ForMethodDescription::isNative))
            .and(not(ModifierReviewable.ForMethodDescription::isSynthetic))
            );

        @Advice.OnMethodEnter
        public static void onEntry(@Advice.Origin Executable method,@Advice.AllArguments Object[] args)  {
            NullChecks.checkArguments(method, args);
        }
    }

    public static class MethodWithReturnValues {

        public static final AsmVisitorWrapper VISITOR = Advice.to(MethodWithReturnValues.class)
            .on(any()
            .and(not(isTypeInitializer()))
            .and(not(returns(VOID)))
            .and(not(ModifierReviewable.ForMethodDescription::isNative))
            .and(not(ModifierReviewable.ForMethodDescription::isSynthetic))
            );

        @Advice.OnMethodExit
        public static void onExit(@Advice.Origin Executable method,@Advice.Return Object returnValue)  {
            NullChecks.checkReturn(method, returnValue);
        }

        // note: using @Advice.This Object thisObject fails for static methods
        // @Advice.AllArguments Object[] args
        @Advice.OnMethodEnter
        public static void onEntry(@Advice.Origin Executable method,@Advice.AllArguments Object[] args)  {
            NullChecks.checkArguments(method, args);
        }
    }

    public static class FieldAccess {

        private static Method FIELD_INTERCEPTOR = null;
        static {
            try {
                FIELD_INTERCEPTOR = FieldAccess.class.getDeclaredMethod("interceptFieldWrite", Object.class,Object.class);
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        public static final AsmVisitorWrapper VISITOR = MemberSubstitution.relaxed()
            .field(ElementMatchers.any())
            .onWrite()
            //.replaceWith(FIELD_INTERCEPTOR)
            .replaceWithChain(

            )
            .on(ElementMatchers.isMethod());

        public static void interceptFieldWrite(Object object,Object value) {
            System.out.println("intercepted field write in object " + object + " , attempt to set value to " + value);
        }
    }

}
