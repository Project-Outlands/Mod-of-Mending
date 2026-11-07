package top.outlands.mod_of_mending.transformer.loliasm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;

/**
 * LoliASM's reflector keeps its own transformer-exclusion set, which the loader never sees; point it at the loader
 * instead, and replace the static initialiser that used to reflect its way to {@code ClassLoader.defineClass}.
 */
public class LoliReflectorTransformer implements IExplicitTransformer {
    private static final String METHOD_HANDLES = "java/lang/invoke/MethodHandles";

    @Override
    public byte[] transform(byte[] bytes) {
        return Bytecode.edit(bytes, node -> {
            for (MethodNode method : node.methods) {
                switch (method.name) {
                    case "<clinit>" -> initialiser(node.name, method);
                    case "removeTransformerExclusion", "addTransformerExclusion" -> exclusion(node.name, method);
                    default -> {
                    }
                }
            }
        });
    }

    private static void initialiser(String owner, MethodNode method) {
        Bytecode.clear(method);
        InsnList code = method.instructions;
        code.add(new MethodInsnNode(Opcodes.INVOKESTATIC, METHOD_HANDLES, "lookup",
                "()Ljava/lang/invoke/MethodHandles$Lookup;", false));
        code.add(new FieldInsnNode(Opcodes.PUTSTATIC, owner, "LOOKUP", "Ljava/lang/invoke/MethodHandles$Lookup;"));

        code.add(new LdcInsnNode(Type.getObjectType("java/lang/ClassLoader")));
        code.add(new LdcInsnNode("defineClass"));
        code.add(new IntInsnNode(Opcodes.BIPUSH, 4));
        code.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Class"));
        int index = 0;
        Type[] parameters = {Type.getObjectType("java/lang/String"), Type.getType("[B"), Type.INT_TYPE, Type.INT_TYPE};
        for (Type parameter : parameters) {
            code.add(new InsnNode(Opcodes.DUP));
            code.add(new InsnNode(Opcodes.ICONST_0 + index++));
            code.add(new LdcInsnNode(parameter));
            code.add(new InsnNode(Opcodes.AASTORE));
        }
        code.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner, "resolveMethod",
                "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;", false));
        code.add(new FieldInsnNode(Opcodes.PUTSTATIC, owner, "classLoader$DefineClass",
                "Ljava/lang/invoke/MethodHandle;"));
        code.add(new InsnNode(Opcodes.RETURN));
        method.maxStack = 6;
    }

    /** {@code Launch.classLoader.<method>(name)} - the loader is where exclusions have to end up. */
    private static void exclusion(String owner, MethodNode method) {
        Bytecode.clear(method);
        method.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraft/launchwrapper/Launch",
                "classLoader", "Lnet/minecraft/launchwrapper/LaunchClassLoader;"));
        // these two are static on this class, so the parameter sits in slot 0 - javassist hid that, the diff caught it
        int slot = (method.access & Opcodes.ACC_STATIC) != 0 ? 0 : 1;
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, slot));
        // the method is declared on the superclass of the field's type, which is also what javassist emitted
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                "top/outlands/foundation/boot/ActualClassLoader", method.name, "(Ljava/lang/String;)V", false));
        method.instructions.add(new InsnNode(Opcodes.RETURN));
        method.maxStack = 2;
    }
}
