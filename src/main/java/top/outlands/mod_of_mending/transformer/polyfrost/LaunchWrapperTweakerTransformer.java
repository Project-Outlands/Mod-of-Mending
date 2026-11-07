package top.outlands.mod_of_mending.transformer.polyfrost;

import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;

/**
 * Forwards the loader queries the tweaker answers to {@code HookHelper}, which resolves them against the actual launch classpath instead of the stage-0 jar the mod shipped with.
 */
public class LaunchWrapperTweakerTransformer implements IExplicitTransformer {
    private static final String HOOK_HELPER = "top/outlands/mod_of_mending/helper/HookHelper";

    @Override
    public byte[] transform(byte[] bytes) {
        bytes = Bytecode.forwardBody(bytes, method -> method.name.equals("isInitialized"), HOOK_HELPER, "isInitialized");
        bytes = Bytecode.forwardBody(bytes, method -> method.name.equals("addToClasspath"), HOOK_HELPER, "addToClasspath");
        return bytes;
    }
}
