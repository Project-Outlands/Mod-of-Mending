package top.outlands.mod_of_mending.transformer.tickcentral;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodNode;
import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;

import java.util.Map;

public class PriorityAppendTransformer implements IExplicitTransformer {
    private static final Map<String, Integer> PRIORITY = Map.of(
            "com.github.terminatornl.tickcentral.asm.BlockTransformer", 1100,
            "com.github.terminatornl.tickcentral.asm.ITickableTransformer", 1200,
            "com.github.terminatornl.tickcentral.asm.EntityTransformer", 1400,
            "com.github.terminatornl.tickcentral.asm.HubAPITransformer", 1500,
            "net.minecraftforge.fml.common.asm.transformers.ModAPITransformer", 1600);

    @Override
    public byte[] transform(byte[] bytes) {
        return Bytecode.edit(bytes, node -> {
            Integer priority = PRIORITY.get(node.name.replace('/', '.'));
            if (priority == null
                    || node.methods.stream().anyMatch(m -> m.name.equals("getPriority") && m.desc.equals("()I"))) {
                return;
            }
            MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "getPriority", "()I", null, null);
            method.instructions.add(new IntInsnNode(Opcodes.SIPUSH, priority));
            method.instructions.add(new InsnNode(Opcodes.IRETURN));
            method.maxStack = 1;
            method.maxLocals = 1;
            node.methods.add(method);
        });
    }
}
