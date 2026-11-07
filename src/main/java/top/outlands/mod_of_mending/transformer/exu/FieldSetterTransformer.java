package top.outlands.mod_of_mending.transformer.exu;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;

/**
 * Extra Utilities 2 resolves the field through its own reflection factory, which no longer exists on a modern JVM.
 * The static initialiser that probed for it is emptied, and the constructor now looks the field up through Forge.
 */
public class FieldSetterTransformer implements IExplicitTransformer {
    private static final String FIELD_TYPE = "Ljava/lang/reflect/Field;";
    private static final String FIND_FIELD = "(Ljava/lang/Class;[Ljava/lang/String;)Ljava/lang/reflect/Field;";

    @Override
    public byte[] transform(byte[] bytes) {
        return Bytecode.edit(bytes, node -> {
            for (MethodNode method : node.methods) {
                if (method.name.equals("<clinit>")) {
                    Bytecode.clear(method);
                    method.instructions.add(new InsnNode(Opcodes.RETURN));
                    method.maxStack = 0;
                } else if (method.name.equals("<init>")) {
                    Bytecode.clear(method);
                    // javassist appended the super() call to the replacement body; without it "this" is uninitialised
                    method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    method.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, node.superName, "<init>",
                            "()V", false));
                    // putfield wants the receiver back on the stack; the super call consumed the first one
                    method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    int slot = 1;
                    for (Type argument : Type.getArgumentTypes(method.desc)) {
                        method.instructions.add(new VarInsnNode(argument.getOpcode(Opcodes.ILOAD), slot));
                        slot += argument.getSize();
                    }
                    method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            "net/minecraftforge/fml/relauncher/ReflectionHelper", "findField", FIND_FIELD, false));
                    method.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, node.name, "field", FIELD_TYPE));
                    method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    method.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, node.name, "field", FIELD_TYPE));
                    method.instructions.add(new InsnNode(Opcodes.ICONST_1));
                    method.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/Field",
                            "setAccessible", "(Z)V", false));
                    method.instructions.add(new InsnNode(Opcodes.RETURN));
                    method.maxStack = Math.max(slot, 3);
                }
            }
        });
    }
}
