package top.outlands.mod_of_mending.transformer.tickcentral;

import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;

public class CompatibilityTransformer implements IExplicitTransformer {
    @Override
    public byte[] transform(byte[] bytes) {
        return Bytecode.emptyBody(bytes, "FixTransformerOrdering");
    }
}
