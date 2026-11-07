package top.outlands.mod_of_mending.transformer.vampirism;

import org.objectweb.asm.Opcodes;
import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;

/** The patch calls into this class from the loader side, so it cannot stay package-private. */
public class TConstructBloodConversionTransformer implements IExplicitTransformer {
    @Override
    public byte[] transform(byte[] bytes) {
        return Bytecode.edit(bytes,
                node -> node.access = (node.access & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED))
                        | Opcodes.ACC_PUBLIC);
    }
}
