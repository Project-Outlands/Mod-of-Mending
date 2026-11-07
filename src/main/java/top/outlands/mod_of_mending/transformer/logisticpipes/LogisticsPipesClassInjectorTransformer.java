package top.outlands.mod_of_mending.transformer.logisticpipes;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;

/** Runs long before every other patch: this handler decides which classes exist in the first place. */
public class LogisticsPipesClassInjectorTransformer implements IExplicitTransformer {
    @Override
    public byte[] transform(byte[] bytes) {
        return Bytecode.addMethod(bytes, Opcodes.ACC_PUBLIC, "getPriority", "()I", method -> {
            method.instructions.add(new IntInsnNode(Opcodes.SIPUSH, -1000));
            method.instructions.add(new InsnNode(Opcodes.IRETURN));
            method.maxStack = 1;
        });
    }
}
