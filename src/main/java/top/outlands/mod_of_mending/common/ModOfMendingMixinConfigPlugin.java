package top.outlands.mod_of_mending.common;

import top.outlands.mod_of_mending.config.ModOfMendingConfig;
import net.minecraft.launchwrapper.Launch;
import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class ModOfMendingMixinConfigPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return switch (mixinClassName.split("\\.")[4]) {
            case "charset" -> ModOfMendingConfig.modPatchConfig.enableCharset;
            case "xaeroplus" -> ModOfMendingConfig.modPatchConfig.enableXP;
            case "codechickenlib" ->
                Launch.classLoader.isClassExist("codechicken.lib.CodeChickenLib")
                        && !Launch.classLoader.isClassExist("codechicken.lib.Reference")
                        && ModOfMendingConfig.modPatchConfig.enableCCL;
            case "minecraftmultipartcbe" -> ModOfMendingConfig.modPatchConfig.enableMultiPart;
            case "projectred_core" -> ModOfMendingConfig.modPatchConfig.enablePR;
            case "solarflux" -> ModOfMendingConfig.modPatchConfig.enableSolarFlux;
            case "custommainmenu" -> ModOfMendingConfig.modPatchConfig.enableCMM;
            case "hammercore" -> ModOfMendingConfig.modPatchConfig.enableHammerCore;
            case "mcjty" -> ModOfMendingConfig.modPatchConfig.enableMcjty;
            case "howlingmoon" -> ModOfMendingConfig.modPatchConfig.enableHowlingMoon;
            case "customnpcs" -> ModOfMendingConfig.modPatchConfig.enableCustomNPC;
            case "waterpower" -> ModOfMendingConfig.modPatchConfig.enableWaterPower;
            case "subaquatic" -> ModOfMendingConfig.modPatchConfig.enableSubaquatic;
            case "armourers_workshop" -> ModOfMendingConfig.modPatchConfig.enableArmourersWorkshop;
            case "mage" -> ModOfMendingConfig.modPatchConfig.enableMage;
            case "extrautils2" -> ModOfMendingConfig.modPatchConfig.enableExtraUtilities;
            case "refinedstorage" ->
                ModOfMendingConfig.modPatchConfig.enableMoreRefinedStorage && ModOfMending.isModNewerThan("refinedstorage", "2.0.0");
            case "carryon" -> ModOfMendingConfig.modPatchConfig.enableCarryon;
            case "betterrecords" -> ModOfMendingConfig.modPatchConfig.enableBetterRecords;
            case "unilib" -> ModOfMendingConfig.modPatchConfig.enableUnilib;
            case "funkylocomotion" -> ModOfMendingConfig.modPatchConfig.enableFunkyLocomotion;
            case "patchouli" -> ModOfMendingConfig.modPatchConfig.enablePatchouli;
            case "reccomplex" ->
                ModOfMendingConfig.modPatchConfig.enableRecurrentComplex
                        && (!mixinClassName.equals("top.outlands.mod_of_mending.mixin.reccomplex.ParametersMixin")
                                || SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_22));
            case "lockdown" -> ModOfMendingConfig.modPatchConfig.enableLockdown;
            case "dissolution" -> ModOfMendingConfig.modPatchConfig.enableDissolution;
            case "glibysphysics" -> ModOfMendingConfig.modPatchConfig.enableGlibysPhysics;
            case "darktheme" -> ModOfMendingConfig.modPatchConfig.enableDarkTheme;
            case "creativecore" -> ModOfMendingConfig.modPatchConfig.enableCreativeCore;
            case "enderutils" -> ModOfMendingConfig.modPatchConfig.enableEnderUtilities;
            default -> true;
        };
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
