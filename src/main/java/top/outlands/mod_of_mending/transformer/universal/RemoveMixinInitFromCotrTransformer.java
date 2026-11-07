package top.outlands.mod_of_mending.transformer.universal;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;

/**
 * Loading plugins of the "call {@code MixinBootstrap.init()} from the constructor" generation crash once the loader
 * has already initialised mixins. The constructor keeps its {@code super()} call and loses everything else.
 *
 * <p>Only touches a constructor that actually calls {@code init} or {@code addConfiguration}, which is what the
 * javassist version did by instrumenting the call sites instead of the constructor.
 */
public class RemoveMixinInitFromCotrTransformer implements IExplicitTransformer {
    @Override
    public byte[] transform(byte[] bytes) {
        return Bytecode.edit(bytes, node -> {
            for (MethodNode method : node.methods) {
                if (!method.name.equals("<init>")) {
                    continue;
                }
                if (!callsMixinInit(method)) {
                    continue;
                }
                Bytecode.clear(method);
                method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                method.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, node.superName, "<init>",
                        "()V", false));
                method.instructions.add(new InsnNode(Opcodes.RETURN));
                method.maxStack = 1;
                return;
            }
        });
    }

    private static boolean callsMixinInit(MethodNode method) {
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof MethodInsnNode call
                    && (call.name.equals("addConfiguration") || call.name.equals("init"))) {
                return true;
            }
        }
        return false;
    }
}
