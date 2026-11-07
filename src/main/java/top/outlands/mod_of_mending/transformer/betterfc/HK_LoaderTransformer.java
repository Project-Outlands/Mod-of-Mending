package top.outlands.mod_of_mending.transformer.betterfc;

import org.objectweb.asm.Opcodes;
import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;

/**
 * Better Formatting Code reaches for {@code Launch.classLoader.transformers} with a private-field helper; hand it the
 * loader's own list instead of a field this loader no longer has.
 */
public class HK_LoaderTransformer implements IExplicitTransformer {
    private static final String TRANSFORMER_DELEGATE = "top/outlands/foundation/TransformerDelegate";

    @Override
    public byte[] transform(byte[] bytes) {
        return Bytecode.replaceCallStatement(bytes, method -> method.name.equals("onConstructed"),
                call -> call.name().equals("getPrivateField"),
                mv -> mv.visitMethodInsn(Opcodes.INVOKESTATIC, TRANSFORMER_DELEGATE, "getTransformers",
                        "()Ljava/util/List;", false));
    }
}
