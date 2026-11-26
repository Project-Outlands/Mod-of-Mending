package top.outlands.mod_of_mending.transformer.universal;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import top.outlands.foundation.IExplicitTransformer;

/**
 * Replaces a ClassWriter subclass' own class hierarchy walk with {@link top.outlands.mod_of_mending.helper.SuperClassHelper}.
 *
 * <p>An override is out of reach of the pack-wide replacement in Foundation, which patches {@code ClassWriter} itself,
 * so each mod that writes its own gets this one. Turns:
 *
 * <pre>
 * protected String getCommonSuperClass(String type1, String type2) { ... }
 * </pre>
 *
 * into
 *
 * <pre>
 * protected String getCommonSuperClass(String type1, String type2) {
 *     return SuperClassHelper.getCommonSuperClass(type1, type2);
 * }
 * </pre>
 *
 * <p>which is symmetric, so the ASM upgrade that changed the order of the merged pair no longer changes the answer,
 * and still loads no classes.
 */
public class GetCommonSuperClassTransformer implements IExplicitTransformer {

    private static final String TARGET_METHOD = "getCommonSuperClass";
    private static final String TARGET_DESCRIPTOR = "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;";
    private static final String HELPER = "top/outlands/mod_of_mending/helper/SuperClassHelper";

    @Override
    public byte[] transform(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        boolean replaced = false;
        for (MethodNode methodNode : classNode.methods) {
            if (!TARGET_METHOD.equals(methodNode.name) || !TARGET_DESCRIPTOR.equals(methodNode.desc)) {
                continue;
            }
            methodNode.instructions.clear();
            methodNode.tryCatchBlocks = null;
            methodNode.localVariables = null;
            methodNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            methodNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
            methodNode.instructions.add(new MethodInsnNode(
                    Opcodes.INVOKESTATIC, HELPER, TARGET_METHOD, TARGET_DESCRIPTOR, false));
            methodNode.instructions.add(new InsnNode(Opcodes.ARETURN));
            methodNode.maxStack = 2;
            replaced = true;
        }

        if (!replaced) {
            return bytes;
        }
        // A straight line needs no frames; the ones of every other method are copied as they were read.
        ClassWriter classWriter = new ClassWriter(0);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }
}
