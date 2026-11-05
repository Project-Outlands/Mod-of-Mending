package top.outlands.mod_of_mending.transformer.loliasm;

import top.outlands.mod_of_mending.common.ModOfMending;
import javassist.ClassPool;
import javassist.CtClass;
import top.outlands.foundation.IExplicitTransformer;
import zone.rong.loliasm.core.LoliLoadingPlugin;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;

public class JavaFixesTransformer implements IExplicitTransformer {
    private boolean shouldRun() {
        String ver;
        try{
            ver = LoliLoadingPlugin.class.getField("VERSION").get(null).toString();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return false;
        }
        ModOfMending.LOGGER.info("Found lolasm version {}", ver);
        return Integer.parseInt(ver.split("\\.")[1]) > 28;
    }
    @Override
    public byte[] transform(byte[] bytes) {
        if (shouldRun()) {
            return bytes;
        }
        try {
            var cp = ClassPool.getDefault();
            CtClass cc = cp.makeClass(new ByteArrayInputStream(bytes));
            cc.getDeclaredMethod("run").setBody("{}");
            bytes = cc.toBytecode();
        } catch (Throwable t) {
            ModOfMending.LOGGER.error("Exception {} on {}", t, this.getClass().getSimpleName());
        }
        return bytes;
    }
}
