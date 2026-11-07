package top.outlands.mod_of_mending.transformer.loliasm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;
import top.outlands.mod_of_mending.common.ModOfMending;
import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;
import zone.rong.loliasm.core.LoliLoadingPlugin;

public class JavaFixesTransformer implements IExplicitTransformer {
    private boolean shouldRun() {
        String ver;
        try{
            ver = LoliLoadingPlugin.class.getField("VERSION").get(null).toString();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return false;
        }
        ModOfMending.LOGGER.info("Found lolasm version {}", ver);
        return Integer.parseInt(ver.split("\\.")[1]) > 28;
    }
    @Override
    public byte[] transform(byte[] bytes) {
        if (shouldRun()) {
            return bytes;
        }
        return Bytecode.emptyBody(bytes, "run");
    }
}
