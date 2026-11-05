package top.outlands.mod_of_mending.transformer.betterfc;

import top.outlands.mod_of_mending.common.ModOfMending;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import top.outlands.foundation.IExplicitTransformer;

import java.io.ByteArrayInputStream;

public class HK_LoaderTransformer implements IExplicitTransformer {

    @Override
    public byte[] transform(byte[] bytes) {
        try {
            CtClass cc = ClassPool.getDefault().makeClass(new ByteArrayInputStream(bytes));
            cc.getDeclaredMethod("onConstructed").instrument(new ExprEditor() {
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("getPrivateField") && m.getLineNumber() == 16) {
                        m.replace("$_ = top.outlands.foundation.TransformerDelegate#getTransformers();");
                    }
                }
            });
            bytes = cc.toBytecode();
            cc.defrost();
        } catch (Throwable t) {
            ModOfMending.LOGGER.error("Exception {} on {}", t, this.getClass().getSimpleName());
        }
        return bytes;
    }
}
