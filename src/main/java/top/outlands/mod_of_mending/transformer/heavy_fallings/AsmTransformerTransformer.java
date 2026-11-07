package top.outlands.mod_of_mending.transformer.heavy_fallings;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;

public class AsmTransformerTransformer implements IExplicitTransformer {
    @Override
    public byte[] transform(byte[] bytes) {
        bytes = Bytecode.replaceBody(bytes,
                method -> method.name.equals("rearrangeThisAfterDeobfuscation"), method -> {
                    Type[] arguments = Type.getArgumentTypes(method.desc);
                    if (arguments.length == 0) {
                        return;
                    }
                    int slot = (method.access & Opcodes.ACC_STATIC) != 0 ? 0 : 1;
                    method.instructions.add(new VarInsnNode(arguments[0].getOpcode(Opcodes.ILOAD), slot));
                    method.instructions.add(
                            new InsnNode(Type.getReturnType(method.desc).getOpcode(Opcodes.IRETURN)));
                    method.maxStack = Math.max(arguments[0].getSize(), Type.getReturnType(method.desc).getSize());
                });
        return Bytecode.addMethod(bytes, Opcodes.ACC_PUBLIC, "getPriority", "()I", method -> {
            method.instructions.add(new IntInsnNode(Opcodes.SIPUSH, 500));
            method.instructions.add(new InsnNode(Opcodes.IRETURN));
            method.maxStack = 1;
        });
    }
}
