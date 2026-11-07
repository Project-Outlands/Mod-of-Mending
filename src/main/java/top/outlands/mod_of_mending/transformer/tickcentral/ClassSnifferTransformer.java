package top.outlands.mod_of_mending.transformer.tickcentral;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;

/**
 * Two independent fixes for TickCentral's class sniffer:
 * <ol>
 *   <li>every {@code Logger.warn} call is dropped - the sniffer logs a warning for each class it cannot map, which
 *       during a transform is thousands of lines nobody can act on;</li>
 *   <li>the {@code ClassReader} in {@code performOnMappedSource} is fed the loader's own view of the class instead of
 *       the sniffer's {@code Compatibility.OrderedArrayList} pass, which would transform the same class twice.</li>
 * </ol>
 */
public class ClassSnifferTransformer implements IExplicitTransformer {
    private static final String HOOK_HELPER = "top/outlands/mod_of_mending/helper/HookHelper";
    private static final String TRANSFORM_PURE = "(Ljava/lang/String;Ljava/lang/String;[B)[B";
    private static final String ORDERED_LIST = "com/github/terminatornl/tickcentral/asm/Compatibility$OrderedArrayList";

    @Override
    public byte[] transform(byte[] bytes) {
        bytes = Bytecode.replaceCallStatement(bytes,
                call -> call.owner().equals("org/apache/logging/log4j/Logger") && call.name().equals("warn"),
                mv -> {});
        return Bytecode.edit(bytes, node -> {
            for (MethodNode method : node.methods) {
                if (method.name.equals("performOnMappedSource")) {
                    feedFromLoader(method);
                }
            }
        });
    }

    /** Replaces the argument of {@code new ClassReader(...)} with {@code HookHelper.transformAsPureFML(...)}. */
    private static void feedFromLoader(MethodNode method) {
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (!(insn instanceof MethodInsnNode call) || call.getOpcode() != Opcodes.INVOKESPECIAL
                    || !call.owner.equals("org/objectweb/asm/ClassReader") || !call.desc.equals("([B)V")) {
                continue;
            }
            // ... GETSTATIC INSTANCE; ALOAD obfName; ALOAD transformedName; ALOAD data; INVOKEVIRTUAL transformAsPureFML
            AbstractInsnNode producer = call.getPrevious();
            AbstractInsnNode data = producer == null ? null : producer.getPrevious();
            AbstractInsnNode transformed = data == null ? null : data.getPrevious();
            AbstractInsnNode obf = transformed == null ? null : transformed.getPrevious();
            AbstractInsnNode instance = obf == null ? null : obf.getPrevious();
            if (!(producer instanceof MethodInsnNode pure) || !pure.name.equals("transformAsPureFML")
                    || !(instance instanceof FieldInsnNode field) || !field.owner.equals(ORDERED_LIST)
                    || !(data instanceof VarInsnNode) || !(transformed instanceof VarInsnNode)
                    || !(obf instanceof VarInsnNode)) {
                return;
            }
            int obfSlot = slot(method, "obfName");
            int transformedSlot = slot(method, "transformedName");
            int dataSlot = slot(method, "data");
            if (obfSlot < 0 || transformedSlot < 0 || dataSlot < 0) {
                return;
            }
            method.instructions.remove(producer);
            method.instructions.remove(instance);
            method.instructions.remove(obf);
            method.instructions.remove(transformed);
            method.instructions.remove(data);
            method.instructions.insertBefore(call, new VarInsnNode(Opcodes.ALOAD, obfSlot));
            method.instructions.insertBefore(call, new VarInsnNode(Opcodes.ALOAD, transformedSlot));
            method.instructions.insertBefore(call, new VarInsnNode(Opcodes.ALOAD, dataSlot));
            method.instructions.insertBefore(call,
                    new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK_HELPER, "transformAsPureFML", TRANSFORM_PURE, false));
            return;
        }
    }

    private static int slot(MethodNode method, String name) {
        if (method.localVariables != null) {
            for (LocalVariableNode variable : method.localVariables) {
                if (variable.name.equals(name)) {
                    return variable.index;
                }
            }
        }
        return -1;
    }
}
