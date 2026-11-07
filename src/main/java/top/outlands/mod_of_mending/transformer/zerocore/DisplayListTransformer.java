package top.outlands.mod_of_mending.transformer.zerocore;

import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;

public class DisplayListTransformer implements IExplicitTransformer {
    @Override
    public byte[] transform(byte[] bytes) {
        return Bytecode.emptyBody(bytes, "finalize");
    }
}
