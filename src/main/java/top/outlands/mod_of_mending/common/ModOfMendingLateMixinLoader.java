package top.outlands.mod_of_mending.common;

import top.outlands.mod_of_mending.config.ModOfMendingConfig;
import com.fuzs.aquaacrobatics.core.AquaAcrobaticsCore;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * So many mods tring to hack Loader instead of using ILateMixinLoader
 */
@SuppressWarnings("deprecation")
public class ModOfMendingLateMixinLoader implements ILateMixinLoader {
    @Override
    public List<String> getMixinConfigs() {
        List<String> configs = new ArrayList<>();
        if (ModOfMendingConfig.modPatchConfig.fixTahumicSpeedup && Loader.isModLoaded("thaumicspeedup") && !ModOfMending.isModNewerThan("thaumicspeedup", "4.0")) {
            configs.add("mixins.thaumicspeedup.json");
            if (Loader.isModLoaded("betterwithmods")) {
                configs.add("mixins.thaumicspeedup_bwmcompat.json");
            }
        }
        if (ModOfMendingConfig.modPatchConfig.enableAquaAcrobatics && Loader.isModLoaded("aquaacrobatics")) {
            AquaAcrobaticsCore.LOGGER.info("Aqua Acrobatics is loading mod compatibility mixins");
            if(Loader.isModLoaded("galacticraftcore")) {
                configs.add("META-INF/mixins.aquaacrobatics.galacticraft.json");
            }
            if(Loader.isModLoaded("journeymap")) {
                ModContainer jmMod = FMLCommonHandler.instance().findContainerFor("journeymap");
                if(jmMod != null) {
                    String version = jmMod.getVersion();
                    if(version.equals("1.12.2-5.5.4")) {
                        configs.add("META-INF/mixins.aquaacrobatics.journeymap55.json");
                    } else if(version.equals("1.12.2-5.7.1")) {
                        configs.add("META-INF/mixins.aquaacrobatics.journeymap57.json");
                    } else {
                        AquaAcrobaticsCore.LOGGER.warn("You have JourneyMap {} installed. Only 1.12.2-5.5.4 and 1.12.2-5.7.1 are patched for water color compatibility.", version);
                    }
                }
            }
            if(Loader.isModLoaded("xaerominimap")) {
                configs.add("META-INF/mixins.aquaacrobatics.xaerosminimap.json");
            }
            if(Loader.isModLoaded("thaumcraft")) {
                configs.add("META-INF/mixins.aquaacrobatics.thaumcraft.json");
            }
            AquaAcrobaticsCore.isModCompatLoaded = true;
        }
        if (ModOfMendingConfig.modPatchConfig.enableIntegratedProxyPatch && Loader.isModLoaded("integrated_proxy")) {
            configs.add("mixins.integrated_proxy.mod.json");
        }
        if (ModOfMendingConfig.modPatchConfig.enableThaumicFixesPatch && Loader.isModLoaded("thaumicfixes")) {
            configs.add("mixins.thaumicfixes.modsupport.json");
        }
        if (ModOfMendingConfig.modPatchConfig.enableErebusFixPatch && Loader.isModLoaded("erebusfix")) {
            configs.add("mixins.erebusfix.json");
        }
        if (ModOfMendingConfig.modPatchConfig.enableUncraftingBlacklist && Loader.isModLoaded("uncrafting_blacklist")) {
            configs.add("mixins.uncraftingblacklist.json");
        }
        if (ModOfMendingConfig.modPatchConfig.enableChiseledMe && Loader.isModLoaded("chiseled_me")) {
            configs.add("chiseled-me.compat.mixins.json");
        }
        return configs;
    }
}
