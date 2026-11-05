package top.outlands.mod_of_mending.transformer.logisticpipes;

import top.outlands.mod_of_mending.common.ModOfMending;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import top.outlands.foundation.IExplicitTransformer;

import java.io.ByteArrayInputStream;

public class LogisticPipesHandlerTransformer implements IExplicitTransformer {
    @Override
    public byte[] transform(byte[] bytes) {
        try {
            var cp = ClassPool.getDefault();
            CtClass cc = cp.makeClass(new ByteArrayInputStream(bytes));
            cc.instrument(new ExprEditor(){
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("exitJava") || m.getMethodName().equals("printStackTrace")) {
                        m.replace("");
                    }
                }
            });
            bytes = cc.toBytecode();
        } catch (Throwable t) {
            ModOfMending.LOGGER.error("Exception {} on {}", t, this.getClass().getSimpleName());
        }
        return bytes;
    }
}
