package top.outlands.mod_of_mending.transformer.tickcentral;

import top.outlands.mod_of_mending.common.ModOfMending;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;
import top.outlands.foundation.IExplicitTransformer;

import java.io.ByteArrayInputStream;

public class ClassSnifferTransformer implements IExplicitTransformer {
    @Override
    public byte[] transform(byte[] bytes) {
        try {
            CtClass cc = ClassPool.getDefault().makeClass(new ByteArrayInputStream(bytes));
            cc.instrument(new ExprEditor(){
                @Override
                public void edit(MethodCall call) throws CannotCompileException {
                    if (call.getClassName().equals("org.apache.logging.log4j.Logger") && call.getMethodName().equals("warn")) {
                        call.replace("{}");
                    }
                }
            });
            cc.getDeclaredMethod("performOnMappedSource").instrument(new ExprEditor(){
                @Override
                public void edit(NewExpr e) throws CannotCompileException {
                    if (e.getLineNumber() == 168 && e.getClassName().equals("org.objectweb.asm.ClassReader")) {
                        e.replace(
                                """
                                $_ = $proceed(top.outlands.mod_of_mending.helper.HookHelper#transformAsPureFML(obfName, transformedName, data));
                                """);
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
