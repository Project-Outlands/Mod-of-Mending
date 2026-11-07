package top.outlands.mod_of_mending.transformer.journeymap;

import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;

public class VersionCheck$1Transformer implements IExplicitTransformer {
    @Override
    public byte[] transform(byte[] bytes) {
        return Bytecode.emptyBody(bytes, "run");
    }
}
