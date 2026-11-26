package top.outlands.mod_of_mending.transformer.universal;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import top.outlands.foundation.IExplicitTransformer;
import top.outlands.mod_of_mending.helper.Bytecode;

/**
 * Gives a listed class the plain no-argument constructor that something else in the load insists on.
 */
public class AddNoArgConstructorTransformer implements IExplicitTransformer {

    @Override
    public byte[] transform(byte[] bytes) {
        return Bytecode.edit(bytes, node -> {
            if ((node.access & (Opcodes.ACC_INTERFACE | Opcodes.ACC_ENUM)) != 0 || node.superName == null) {
                return;
            }
            for (MethodNode method : node.methods) {
                if (method.name.equals("<init>") && method.desc.equals("()V")) {
                    method.access =
                            (method.access & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED)) | Opcodes.ACC_PUBLIC;
                    return;
                }
            }
            MethodNode constructor = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
            constructor.maxLocals = 1;
            constructor.maxStack = 1;
            constructor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            constructor.instructions.add(
                    new MethodInsnNode(Opcodes.INVOKESPECIAL, node.superName, "<init>", "()V", false));
            constructor.instructions.add(new InsnNode(Opcodes.RETURN));
            node.methods.add(constructor);
        });
    }
}