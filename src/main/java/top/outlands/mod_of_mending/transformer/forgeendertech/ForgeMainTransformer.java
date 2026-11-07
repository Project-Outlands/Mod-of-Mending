package top.outlands.mod_of_mending.transformer.forgeendertech;

import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;

/**
 * ForgeEndertech's integrity check reads its own jar signature and refuses to start when it cannot. Only that call is
 * dropped; {@code super.initClient(event)} and everything else in the method keep running.
 */
public class ForgeMainTransformer implements IExplicitTransformer {
    private static final String CHECK = "com/endertech/minecraft/forge/versions/Dependencies";

    @Override
    public byte[] transform(byte[] bytes) {
        return Bytecode.replaceCallStatement(bytes, method -> method.name.equals("initClient"),
                call -> call.owner().equals(CHECK) && call.name().equals("checkIntegrityOnce"), mv -> {});
    }
}
