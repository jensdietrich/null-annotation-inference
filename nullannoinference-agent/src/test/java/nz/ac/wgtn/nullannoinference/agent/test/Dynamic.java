package nz.ac.wgtn.nullannoinference.agent.test;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import nz.ac.wgtn.nullannoinference.agent.NullChecks;

/**
 * Utility to dynamically instrument classes used in tests.
 * @author jens dietrich
 */
public class Dynamic {
    static {
        ByteBuddyAgent.install();
    }
    static void transform(Class clazz) {
        ClassReloadingStrategy classReloadingStrategy = ClassReloadingStrategy.fromInstalledAgent(ClassReloadingStrategy.Strategy.REDEFINITION);
        new ByteBuddy()
            .redefine(clazz)
            .visit(NullChecks.MethodWithoutReturnValues.VISITOR)
            .visit(NullChecks.MethodWithReturnValues.VISITOR)
            .make()
            .load(clazz.getClassLoader(), classReloadingStrategy);
    }
}
