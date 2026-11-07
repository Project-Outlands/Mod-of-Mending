package top.outlands.mod_of_mending.transformer.integrated_proxy;

import top.outlands.mod_of_mending.helper.Bytecode;
import top.outlands.foundation.IExplicitTransformer;

/**
 * The proxy's mixin loader calls {@code MixinBootstrap.init()} from its loading plugin constructor; MixinBooter owns
 * that now, so the constructor is reduced to {@code super()}.
 */
public class MixinLoaderTransformer implements IExplicitTransformer {
    @Override
    public byte[] transform(byte[] bytes) {
        return Bytecode.emptyConstructor(bytes, method -> true);
    }
}
