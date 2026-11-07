package top.outlands.mod_of_mending.transformer.subaquatic;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;

/**
 * Subaquatic injects {@code new VarInsnNode(ALOAD, obfuscated ? 71 : 26)} into {@code Entity.func_70091_d}: slot 71 is
 * where the {@code blockpos} local sits in the Mojang-compiled body, slot 26 is where it sits in the MCP-recompiled
 * one.
 *
 * <p>This loader renames to srg, so the plugin sees {@code obfuscated == true} and picks 71, but the body it actually
 * transforms is the MCP one: {@code func_70091_d} has {@code max_locals = 61} there, so {@code aload 71} is out of
 * range and the class fails to verify the moment an entity loads. The condition is forced onto the 26 branch, which is
 * the slot that exists in the body we run.
 */
public class PluginEntityTransformer implements IExplicitTransformer {
    private static final String VAR_INSN_NODE = "org/objectweb/asm/tree/VarInsnNode";

    @Override
    public byte[] transform(byte[] bytes) {
        return Bytecode.edit(bytes, node -> {
            for (MethodNode method : node.methods) {
                if (method.name.equals("transform")) {
                    forceDeobfSlot(method);
                }
            }
        });
    }

    private static void forceDeobfSlot(MethodNode method) {
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (!(insn instanceof MethodInsnNode call) || call.getOpcode() != Opcodes.INVOKESPECIAL
                    || !call.owner.equals(VAR_INSN_NODE) || !call.desc.equals("(II)V")) {
                continue;
            }
            // ... bipush 25; iload obfuscated; ifeq ->26; bipush 71; goto ->end; ->26: bipush 26; end: invokespecial
            JumpInsnNode branch = null;
            for (AbstractInsnNode cursor = call.getPrevious(); cursor != null; cursor = cursor.getPrevious()) {
                if (cursor instanceof JumpInsnNode jump && jump.getOpcode() == Opcodes.IFEQ) {
                    branch = jump;
                    break;
                }
            }
            if (branch == null || !hasConstant(branch, call, 71) || !hasConstant(branch, call, 26)) {
                continue;
            }
            AbstractInsnNode condition = real(branch.getPrevious());
            if (!(condition instanceof VarInsnNode)) {
                continue;
            }
            method.instructions.set(condition, new InsnNode(Opcodes.ICONST_0));
        }
    }

    /** whether {@code value} is pushed between {@code from} and {@code to} */
    private static boolean hasConstant(AbstractInsnNode from, AbstractInsnNode to, int value) {
        for (AbstractInsnNode cursor = from.getNext(); cursor != null && cursor != to; cursor = cursor.getNext()) {
            if (cursor instanceof IntInsnNode constant && constant.operand == value) {
                return true;
            }
        }
        return false;
    }

    private static AbstractInsnNode real(AbstractInsnNode insn) {
        AbstractInsnNode cursor = insn;
        while (cursor != null && cursor.getOpcode() < 0) {
            cursor = cursor.getPrevious();
        }
        return cursor;
    }
}
