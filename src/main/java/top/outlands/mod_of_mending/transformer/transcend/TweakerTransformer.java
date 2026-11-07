package top.outlands.mod_of_mending.transformer.transcend;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;

public class TweakerTransformer implements IExplicitTransformer {
    @Override
    public byte[] transform(byte[] bytes) {
        return Bytecode.replaceBody(bytes, method -> method.name.equals("getLaunchArguments"), method -> {
            method.instructions.add(new InsnNode(Opcodes.ICONST_0));
            method.instructions.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/String"));
            method.instructions.add(new InsnNode(Opcodes.ARETURN));
            method.maxStack = 1;
        });
    }
}
