package top.outlands.mod_of_mending.transformer.allmusic_client;

import top.outlands.mod_of_mending.common.ModOfMending;
import net.minecraft.launchwrapper.Launch;
import org.apache.commons.io.IOUtils;
import top.outlands.foundation.IExplicitTransformer;

import java.io.IOException;
import java.io.InputStream;

public class AllMusicPlayerTransformer implements IExplicitTransformer {
    @Override
    public byte[] transform(byte[] bytes) {
        try (InputStream inputStream = getClass().getResourceAsStream("/patches/AllMusicPlayer.bin")) {
            if (inputStream != null) {
                return IOUtils.toByteArray(inputStream);
            } else {
                ModOfMending.LOGGER.error("Resource is null");
                return null;
            }
        } catch (IOException e) {
            ModOfMending.LOGGER.error(e.getLocalizedMessage());
            return null;
        }
    }
}
