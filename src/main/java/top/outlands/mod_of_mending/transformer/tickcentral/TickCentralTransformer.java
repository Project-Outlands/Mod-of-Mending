package top.outlands.mod_of_mending.transformer.tickcentral;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;

/**
 * TickCentral has to be told to rebuild its transformer list before it hands it to the loader; the javassist version
 * appended that call to the end of {@code getASMTransformerClass}, i.e. before every return.
 */
public class TickCentralTransformer implements IExplicitTransformer {
    private static final String HOOK_HELPER = "top/outlands/mod_of_mending/helper/HookHelper";

    @Override
    public byte[] transform(byte[] bytes) {
        return Bytecode.edit(bytes, node -> {
            for (var method : node.methods) {
                if (!method.name.equals("getASMTransformerClass")) {
                    continue;
                }
                for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                    int opcode = insn.getOpcode();
                    if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                        method.instructions.insertBefore(insn, new MethodInsnNode(Opcodes.INVOKESTATIC,
                                HOOK_HELPER, "TickCentralPreLoad", "()V", false));
                    }
                }
            }
        });
    }
}
