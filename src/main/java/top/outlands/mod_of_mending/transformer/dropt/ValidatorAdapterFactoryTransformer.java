package top.outlands.mod_of_mending.transformer.dropt;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;

/**
 * Dropt reads {@code boundFields} out of Gson's adapter by reflection, which no longer opens up on a modern JVM. The
 * whole method degrades to the plain delegate lookup it was trying to reach into.
 */
public class ValidatorAdapterFactoryTransformer implements IExplicitTransformer {
    private static final String GET_DELEGATE = "(Lcom/google/gson/TypeAdapterFactory;"
            + "Lcom/google/gson/reflect/TypeToken;)Lcom/google/gson/TypeAdapter;";

    @Override
    public byte[] transform(byte[] bytes) {
        return Bytecode.replaceBody(bytes, method -> method.name.equals("create"), method -> {
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
            method.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "com/google/gson/Gson",
                    "getDelegateAdapter", GET_DELEGATE, false));
            method.instructions.add(new InsnNode(Opcodes.ARETURN));
            method.maxStack = 3;
        });
    }
}
