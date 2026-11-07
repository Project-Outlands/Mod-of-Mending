package top.outlands.mod_of_mending.transformer.transcend;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;
import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;

/**
 * Transcend calls {@code URLClassLoader.addURL} reflectively; the call has to report success without doing anything,
 * because the loader it targets no longer has a modifiable URL list.
 */
public class FMLLoadingPluginTransformer implements IExplicitTransformer {
    @Override
    public byte[] transform(byte[] bytes) {
        return Bytecode.replaceCallStatement(bytes, method -> method.name.equals("addTweaker"),
                call -> call.owner().equals("java/lang/reflect/Method") && call.name().equals("invoke"),
                mv -> mv.visitInsn(Opcodes.ACONST_NULL));
    }
}
