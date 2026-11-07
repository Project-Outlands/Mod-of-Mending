package top.outlands.mod_of_mending.transformer.loliasm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;

/**
 * LoliASM attributes a class to a mod by looking up its resource URL. On a modern JVM every JDK class resolves to a
 * {@code jrt:} URL, which cannot be matched to a mod and is not worth the work - the lookup returns an empty set for
 * those.
 *
 * <p>The javassist version inserted this at a fixed bytecode offset (64), which lands in the middle of unrelated code
 * as soon as the mod is rebuilt. This one hangs off the resource assignment and jumps to the method's existing
 * "return empty set" block, so no stack map frame has to be invented.
 */
public class ModIdentifierTransformer implements IExplicitTransformer {
    private static final String IDENTIFY = "identifyFromClass";
    private static final String IDENTIFY_DESC = "(Ljava/lang/String;Ljava/util/Map;)Ljava/util/Set;";

    @Override
    public byte[] transform(byte[] bytes) {
        return Bytecode.edit(bytes, node -> {
            for (MethodNode method : node.methods) {
                if (method.name.equals(IDENTIFY) && method.desc.equals(IDENTIFY_DESC)) {
                    guardAgainstJrt(method);
                }
            }
        });
    }

    private static void guardAgainstJrt(MethodNode method) {
        LabelNode emptySet = emptySetTarget(method);
        if (emptySet == null) {
            return;
        }
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (!(insn instanceof MethodInsnNode call) || !call.name.equals("getResource")) {
                continue;
            }
            AbstractInsnNode store = call.getNext();
            if (!(store instanceof VarInsnNode astore) || astore.getOpcode() != Opcodes.ASTORE) {
                continue;
            }
            // if (url.getProtocol().equals("jrt")) return Collections.emptySet();
            InsnList guard = new InsnList();
            guard.add(new VarInsnNode(Opcodes.ALOAD, astore.var));
            guard.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/net/URL", "getProtocol",
                    "()Ljava/lang/String;", false));
            guard.add(new LdcInsnNode("jrt"));
            guard.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals",
                    "(Ljava/lang/Object;)Z", false));
            guard.add(new JumpInsnNode(Opcodes.IFNE, emptySet));
            method.instructions.insert(store, guard);
            return;
        }
    }

    /** The label in front of the existing {@code return Collections.emptySet()}. */
    private static LabelNode emptySetTarget(MethodNode method) {
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode call && call.owner.equals("java/util/Collections")
                    && call.name.equals("emptySet")) {
                AbstractInsnNode previous = insn.getPrevious();
                while (previous != null && !(previous instanceof LabelNode)) {
                    previous = previous.getPrevious();
                }
                // the frame the verifier needs already exists there, which is why no new one is introduced
                return previous instanceof LabelNode label ? label : null;
            }
        }
        return null;
    }
}
