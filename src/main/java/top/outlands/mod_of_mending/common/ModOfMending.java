package top.outlands.mod_of_mending.common;

import top.outlands.mod_of_mending.Reference;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.artifact.versioning.ComparableVersion;

import java.util.Map;

@Mod(
        modid = Reference.MOD_ID,
        name = Reference.MOD_NAME,
        useMetadata = true,
        version = Reference.VERSION,
        dependencies = Reference.MOD_DEPENDENCIES,
        acceptableRemoteVersions = "*"
)
public class ModOfMending {

    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_NAME);

    public static boolean isModNewerThan(String modid, String version) {
        var mod = Loader.instance().getIndexedModList().get(modid);
        return new ComparableVersion(mod.getVersion()).compareTo(new ComparableVersion(version)) > 0;
    }
	
	@Instance(Reference.MOD_ID)
	public static ModOfMending _instance;
    public ModOfMending() {

    }

    @NetworkCheckHandler
    public boolean checker(Map<String, String> mods, Side side) {
        return true;
    }

}
