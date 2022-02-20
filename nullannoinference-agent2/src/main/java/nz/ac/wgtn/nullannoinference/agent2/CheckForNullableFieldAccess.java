package nz.ac.wgtn.nullannoinference.agent2;

import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.IssueStore;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Method;

/**
 * Instrument field writes, insert log statement.
 * @author jens dietrich
 */
public class CheckForNullableFieldAccess extends ClassVisitor {

    public static final int VERSION = Opcodes.ASM9;

    public static final String CONTEXT = System.getProperty("nz.ac.wgtn.nullannoinference.context");

    static {
        System.out.println(CheckForNullableFieldAccess.class.getName() + "::CONTEXT set to " + CONTEXT);
    }

    public CheckForNullableFieldAccess(ClassWriter writer) {
        super(VERSION,writer);
    }
    private String className = null;


    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
    }


    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature,exceptions);

        return new LogFieldAccessVisitor(mv);
    }

    static class LogFieldAccessVisitor extends MethodVisitor {
        private MethodVisitor mv = null;
        public LogFieldAccessVisitor(MethodVisitor mv) {
            super(VERSION, mv);
            this.mv = mv;
        }


        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {

            if (descriptor.startsWith("L") || descriptor.startsWith("[")) {
                mv.visitInsn(Opcodes.DUP);
                mv.visitLdcInsn(owner);
                mv.visitLdcInsn(name);
                mv.visitLdcInsn(descriptor);
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, CheckForNullableFieldAccess.class.getName().replace('.', '/'), "fieldAccessLogged", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
            }
            super.visitFieldInsn(opcode, owner, name, descriptor);
        }
    }

    public static void fieldAccessLogged(Object value, String clazz, String name, String descriptor) {
//        System.out.println("field value logged");
//        System.out.println("\towner: " + clazz);
//        System.out.println("\tfield name: " + name);
//        System.out.println("\tdescriptor: " + descriptor);
//        System.out.println("\tvalue: " + value);
        if (value==null) {
            Issue issue = new Issue(clazz.replace('/','.'), name, descriptor, CONTEXT, Issue.IssueType.FIELD, -1);
            IssueStore.add(issue);
        }

    }




}
