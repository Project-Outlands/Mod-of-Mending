package top.outlands.mod_of_mending.transformer.journeymap;

import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;

/**
 * JourneyMap deletes its cache with a call that fails on newer JVMs; {@code HookHelper.deleteFile} falls back to a plain filesystem delete.
 */
public class FileHandlerTransformer implements IExplicitTransformer {
    private static final String HOOK_HELPER = "top/outlands/mod_of_mending/helper/HookHelper";

    @Override
    public byte[] transform(byte[] bytes) {
        bytes = Bytecode.forwardBody(bytes, method -> method.name.equals("delete"), HOOK_HELPER, "deleteFile");
        return bytes;
    }
}
