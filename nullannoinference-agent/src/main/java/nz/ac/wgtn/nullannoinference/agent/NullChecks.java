package nz.ac.wgtn.nullannoinference.agent;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.asm.MemberSubstitution;
import net.bytebuddy.description.ModifierReviewable;
import net.bytebuddy.matcher.ElementMatchers;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.IssueStore;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
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

    public static final String CONTEXT = System.getProperty("nz.ac.wgtn.nullannoinference.context");

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

    // check the state of object using reflection
    public static void checkThis(Executable method, Object object){
     //   if ((method instanceof Constructor)) { // also check synthetic methods as the target is to annotate fields, not methods
            // todo: extend this to super classes within scope (need to match against prefix)
            for (Field field:object.getClass().getDeclaredFields()) {
                if (!field.getType().isPrimitive()) {
                    try {
                        field.setAccessible(true);
                        Object value = field.get(object);
                        if (value==null) {
                            Issue issue = new Issue(field.getDeclaringClass().getName(), field.getName(), getTypeNameInByteCodeFormat(field.getType()), CONTEXT, Issue.IssueType.FIELD, -1);
                            IssueStore.add(issue);
                        }
                    } catch (Exception x) {
                        x.printStackTrace();
                    }
                }
            }
       // }
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

    public static class Constructors {

        public static final AsmVisitorWrapper VISITOR = Advice.to(Constructors.class)
            .on(any()
            .and(isConstructor())
            );

        @Advice.OnMethodExit
        public static void onExit(@Advice.Origin Executable method,@Advice.This Object object)  {
            System.out.println("constructor exit");
            NullChecks.checkThis(method, object);
        }

    }

}
