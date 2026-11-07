package top.outlands.mod_of_mending.transformer.logisticpipes;

import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;

/**
 * LogisticsPipes defines its generated classes through a reflective {@code defineClass} that a modern JVM refuses; {@code HookHelper.defineClass} uses the loader instead.
 */
public class defineClassTransformer implements IExplicitTransformer {
    private static final String HOOK_HELPER = "top/outlands/mod_of_mending/helper/HookHelper";

    @Override
    public byte[] transform(byte[] bytes) {
        bytes = Bytecode.forwardBody(bytes, method -> method.name.equals("loadClass"), HOOK_HELPER, "defineClass");
        return bytes;
    }
}
