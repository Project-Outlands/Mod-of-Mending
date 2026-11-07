package top.outlands.mod_of_mending.transformer.sereneseasons;

import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;

/**
 * SereneSeasons clears instructions differently depending on the ASM level it was compiled against.
 */
public class ASMHelperTransformer implements IExplicitTransformer {
    private static final String HOOK_HELPER = "top/outlands/mod_of_mending/helper/HookHelper";

    @Override
    public byte[] transform(byte[] bytes) {
        bytes = Bytecode.forwardBody(bytes, method -> method.name.equals("clearNextInstructions"), HOOK_HELPER, "clearNextInstructions");
        return bytes;
    }
}
