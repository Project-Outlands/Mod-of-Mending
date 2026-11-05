package top.outlands.mod_of_mending.transformer.vampirism;

import top.outlands.mod_of_mending.common.ModOfMending;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;
import top.outlands.foundation.IExplicitTransformer;

import java.io.ByteArrayInputStream;

public class WorldGenVampireOrchidTransformer implements IExplicitTransformer {
    @Override
    public byte[] transform(byte[] bytes) {
        try {
            CtClass cc = ClassPool.getDefault().makeClass(new ByteArrayInputStream(bytes));
            cc.setModifiers(cc.getModifiers() - Modifier.PRIVATE + Modifier.PUBLIC);
            CtField flower = cc.getField("flower");
            flower.setModifiers((flower.getModifiers() | Modifier.FINAL | Modifier.PUBLIC) - Modifier.PRIVATE);
            bytes = cc.toBytecode();
        } catch (Throwable t) {
            ModOfMending.LOGGER.error("Exception {} on {}", t, this.getClass().getSimpleName());
        }
        return bytes;
    }
}
