package top.outlands.mod_of_mending.transformer.farseek;

import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;

public class FarseekClassTransformerTransformer implements IExplicitTransformer {
    @Override
    public byte[] transform(byte[] bytes) {
        bytes = Bytecode.emptyBody(bytes, "moveAfterSponge");
        return Bytecode.addMethod(bytes, org.objectweb.asm.Opcodes.ACC_PUBLIC, "getPriority", "()I", method -> {
            method.instructions.add(new IntInsnNode(Opcodes.SIPUSH, 2000));
            method.instructions.add(new InsnNode(Opcodes.IRETURN));
            method.maxStack = 1;
        });
    }
}
