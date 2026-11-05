package top.outlands.mod_of_mending.transformer.polyfrost;

import top.outlands.mod_of_mending.common.ModOfMending;
import javassist.ClassPool;
import javassist.CtClass;
import top.outlands.foundation.IExplicitTransformer;

import java.io.ByteArrayInputStream;

public class LaunchWrapperTweakerTransformer implements IExplicitTransformer {
    @Override
    public byte[] transform(byte[] bytes) {
        try {
            CtClass cc = ClassPool.getDefault().makeClass(new ByteArrayInputStream(bytes));
            cc.getDeclaredMethod("isInitialized").setBody("{return top.outlands.mod_of_mending.helper.HookHelper#isInitialized($$);}");
            cc.getDeclaredMethod("addToClasspath").setBody("{top.outlands.mod_of_mending.helper.HookHelper#addToClasspath($$);}");
            bytes = cc.toBytecode();
        } catch (Throwable t) {
            ModOfMending.LOGGER.error("Exception {} on {}", t, this.getClass().getSimpleName());
        }
        return bytes;
    }
}
