package top.outlands.mod_of_mending.transformer.logisticpipes;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;

/**
 * LogisticsPipes resolves its own classes through {@code LogisticsPipes.class.getClassLoader()}; during
 * pre-initialisation that loader is still busy with LogisticsPipes, so the lookup deadlocks or hands back a class that
 * is not ready. Every such call goes through the launch class loader instead.
 */
public class LoadClassTransformer implements IExplicitTransformer {
    @Override
    public byte[] transform(byte[] bytes) {
        return Bytecode.edit(bytes, node -> {
            for (MethodNode method : node.methods) {
                rewrite(method);
            }
        });
    }

    private static void rewrite(MethodNode method) {
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (!(insn instanceof MethodInsnNode call) || call.getOpcode() != Opcodes.INVOKEVIRTUAL
                    || !call.name.equals("loadClass")) {
                continue;
            }
            // <receiver> <argument>... invokevirtual loadClass - the arguments sit between the two
            AbstractInsnNode receiver = call.getPrevious();
            for (int i = 0; i < Type.getArgumentTypes(call.desc).length && receiver != null; i++) {
                if (!(receiver instanceof VarInsnNode)) {
                    receiver = null;
                    break;
                }
                receiver = receiver.getPrevious();
            }
            AbstractInsnNode constant = receiver == null ? null : receiver.getPrevious();
            if (!(receiver instanceof MethodInsnNode loader) || !loader.name.equals("getClassLoader")
                    || !(constant instanceof LdcInsnNode)) {
                continue;
            }
            // the receiver has to be on the stack *below* the arguments, so it goes where the old one stood
            AbstractInsnNode firstArgument = receiver.getNext();
            method.instructions.remove(constant);
            method.instructions.remove(receiver);
            method.instructions.insertBefore(firstArgument, new FieldInsnNode(Opcodes.GETSTATIC,
                    "net/minecraft/launchwrapper/Launch", "classLoader",
                    "Lnet/minecraft/launchwrapper/LaunchClassLoader;"));
        }
    }
}
