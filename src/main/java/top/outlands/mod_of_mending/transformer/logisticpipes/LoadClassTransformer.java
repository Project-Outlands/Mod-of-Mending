package top.outlands.mod_of_mending.transformer.logisticpipes;

import top.outlands.mod_of_mending.common.ModOfMending;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import net.minecraft.launchwrapper.Launch;
import top.outlands.foundation.IExplicitTransformer;

import java.io.ByteArrayInputStream;

public class LoadClassTransformer implements IExplicitTransformer {
    @Override
    public byte[] transform(byte[] bytes) {
        try {
            CtClass cc = ClassPool.getDefault().makeClass(new ByteArrayInputStream(bytes));
            cc.instrument(new ExprEditor(){
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if(m.getMethodName().equals("loadClass")) {
                        m.replace("$_ = net.minecraft.launchwrapper.Launch#classLoader.loadClass($$);");
                    }
                }
            });

            bytes = cc.toBytecode();
        }catch (Throwable t) {
            ModOfMending.LOGGER.error("Exception {} on {}", t, this.getClass().getSimpleName());
        }
        return bytes;
    }
}
