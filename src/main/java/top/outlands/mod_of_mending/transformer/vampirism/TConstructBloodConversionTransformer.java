package top.outlands.mod_of_mending.transformer.vampirism;

import top.outlands.mod_of_mending.common.ModOfMending;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;
import top.outlands.foundation.IExplicitTransformer;

import java.io.ByteArrayInputStream;

public class TConstructBloodConversionTransformer implements IExplicitTransformer {
    @Override
    public byte[] transform(byte[] bytes) {

        try {
            CtClass cc = ClassPool.getDefault().makeClass(new ByteArrayInputStream(bytes));
            cc.setModifiers(Modifier.setPublic(cc.getModifiers()));
            bytes = cc.toBytecode();
            cc.defrost();
        } catch (Throwable t) {
            ModOfMending.LOGGER.error("Exception {} on {}", t, this.getClass().getSimpleName());
        }
        return bytes;
    }
}
