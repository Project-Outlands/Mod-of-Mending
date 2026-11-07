package top.outlands.mod_of_mending.transformer.vampirism;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldNode;
import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;

/** The patch builds this generator from outside, so both the class and its flower field have to be reachable. */
public class WorldGenVampireOrchidTransformer implements IExplicitTransformer {
    @Override
    public byte[] transform(byte[] bytes) {
        return Bytecode.edit(bytes, node -> {
            node.access = (node.access & ~Opcodes.ACC_PRIVATE) | Opcodes.ACC_PUBLIC;
            for (FieldNode field : node.fields) {
                if (field.name.equals("flower")) {
                    field.access = (field.access | Opcodes.ACC_FINAL | Opcodes.ACC_PUBLIC) & ~Opcodes.ACC_PRIVATE;
                }
            }
        });
    }
}
