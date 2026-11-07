package top.outlands.mod_of_mending.transformer.simplehotspring;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;

/**
 * The config builds a fastutil map with a load factor of 1.0, which newer fastutil treats as a request for a table
 * that cannot grow. Only the factor changes - the capacity argument stays whatever the mod asked for.
 */
public class SimplyHotSpringsConfigTransformer implements IExplicitTransformer {
    private static final String MAP = "it/unimi/dsi/fastutil/objects/Object2ObjectOpenHashMap";

    @Override
    public byte[] transform(byte[] bytes) {
        return Bytecode.edit(bytes, node -> {
            for (MethodNode method : node.methods) {
                if (!method.name.equals("<clinit>")) {
                    continue;
                }
                for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                    if (!(insn instanceof MethodInsnNode call) || call.getOpcode() != Opcodes.INVOKESPECIAL
                            || !call.owner.equals(MAP) || !call.desc.equals("(IF)V")) {
                        continue;
                    }
                    AbstractInsnNode factor = call.getPrevious();
                    if (!(factor instanceof LdcInsnNode) && !(factor instanceof org.objectweb.asm.tree.InsnNode)) {
                        continue;
                    }
                    method.instructions.remove(factor);
                    method.instructions.insertBefore(call, new LdcInsnNode(0.75F));
                }
            }
        });
    }
}
