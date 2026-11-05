package top.outlands.mod_of_mending.transformer.mobends;

import top.outlands.mod_of_mending.common.ModOfMending;
import top.outlands.mod_of_mending.transformer.universal.RemapTransformer;
import net.minecraft.launchwrapper.Launch;
import top.outlands.foundation.IExplicitTransformer;

import java.io.IOException;

public class ConnectionHelperTransformation implements IExplicitTransformer {
    @Override
    public byte[] transform(byte[] basicClass) {
        try {
            return new PackageRemapper().transform(Launch.classLoader.getClassBytes("top.outlands.mod_of_mending.helper.ConnectionHelper"));
        } catch (IOException e) {
            ModOfMending.LOGGER.error("Mo'Bends class replacing failed.", e);
            return basicClass;
        }
    }

    private static class PackageRemapper extends RemapTransformer {

        public PackageRemapper() {
            super(new String[]{"top/outlands/mod_of_mending/helper"}, new String[]{"goblinbob/mobends/core/util"});
        }
    }
}
