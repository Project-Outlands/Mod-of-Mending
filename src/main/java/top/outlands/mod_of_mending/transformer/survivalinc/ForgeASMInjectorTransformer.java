package top.outlands.mod_of_mending.transformer.survivalinc;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;

/**
 * Survival Inc. builds a reflective handle on {@code Launch.classLoader.defineClass} in its constructor and uses it to
 * inject classes; both halves are replaced with the direct call, so the constructor has nothing left to do.
 */
public class ForgeASMInjectorTransformer implements IExplicitTransformer {
    private static final String LOADER = "top/outlands/foundation/boot/ActualClassLoader";

    @Override
    public byte[] transform(byte[] bytes) {
        bytes = Bytecode.emptyConstructor(bytes, method -> true);
        return Bytecode.replaceBody(bytes, method -> method.name.equals("injectClass"), method -> {
            method.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraft/launchwrapper/Launch",
                    "classLoader", "Lnet/minecraft/launchwrapper/LaunchClassLoader;"));
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
            method.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, LOADER, "defineClass",
                    "(Ljava/lang/String;[B)Ljava/lang/Class;", false));
            method.instructions.add(new InsnNode(Opcodes.ARETURN));
            method.maxStack = 3;
        });
    }
}
