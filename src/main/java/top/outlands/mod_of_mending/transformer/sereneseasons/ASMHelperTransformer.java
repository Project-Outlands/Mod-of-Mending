package top.outlands.mod_of_mending.transformer.sereneseasons;

import top.outlands.mod_of_mending.common.ModOfMending;
import javassist.ClassPool;
import javassist.CtClass;
import top.outlands.foundation.IExplicitTransformer;

import java.io.ByteArrayInputStream;

public class ASMHelperTransformer implements IExplicitTransformer {
    @Override
    public byte[] transform(byte[] bytes) {
        try {
            CtClass cc = ClassPool.getDefault().makeClass(new ByteArrayInputStream(bytes));
            cc.getMethod(
                            "clearNextInstructions",
                            "(Lorg/objectweb/asm/tree/MethodNode;Lorg/objectweb/asm/tree/AbstractInsnNode;I)V")
                    .setBody("{top.outlands.mod_of_mending.helper.HookHelper.clearNextInstructions($$);}");
            bytes = cc.toBytecode();
            cc.defrost();
        } catch (Throwable t) {
            ModOfMending.LOGGER.error("Exception {} on {}", t, this.getClass().getSimpleName());
        }
        return bytes;
    }
}
