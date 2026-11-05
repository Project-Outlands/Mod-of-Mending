package top.outlands.mod_of_mending.transformer.customskinloader;

import top.outlands.mod_of_mending.common.ModOfMending;
import javassist.ClassPool;
import javassist.CtClass;
import top.outlands.foundation.IExplicitTransformer;

import java.io.ByteArrayInputStream;

public class ForgeTweakerTransformer implements IExplicitTransformer {
    @Override
    public byte[] transform(byte[] bytes) {
        try {
            CtClass cc = ClassPool.getDefault().makeClass(new ByteArrayInputStream(bytes));
            cc.getConstructors()[0].setBody(
                    """
                    {customskinloader.forge.platform.IFMLPlatform$FMLPlatformInitializer#initFMLPlatform();}
                    """);
            bytes = cc.toBytecode();
        } catch (Throwable t) {
            ModOfMending.LOGGER.error("Exception {} on {}", t, this.getClass().getSimpleName());
        }
        return bytes;
    }
}
