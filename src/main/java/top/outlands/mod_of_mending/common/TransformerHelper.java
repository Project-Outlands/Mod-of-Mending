package top.outlands.mod_of_mending.common;

import top.outlands.mod_of_mending.config.ModOfMendingConfig;
import top.outlands.mod_of_mending.transformer.advancedrocket.ClassTransformerTransformer;
import top.outlands.mod_of_mending.transformer.aether.ClientProxyTransformer;
import top.outlands.mod_of_mending.transformer.allmusic_client.AllMusicHudTransformer;
import top.outlands.mod_of_mending.transformer.allmusic_client.AllMusicPlayerTransformer;
import top.outlands.mod_of_mending.transformer.betterfc.HK_LoaderTransformer;
import top.outlands.mod_of_mending.transformer.bettersprinting.TransformerEntityPlayerSPTransformer;
import top.outlands.mod_of_mending.transformer.chiseled_me.ChiseledMeLoaderTransformer;
import top.outlands.mod_of_mending.transformer.colytra.EntityLivingBaseTransformer;
import top.outlands.mod_of_mending.transformer.corpse.MessageOpenHistoryTransformer;
import top.outlands.mod_of_mending.transformer.crossbow.TransformerEntityArrowTransformer;
import top.outlands.mod_of_mending.transformer.customskinloader.ForgeTweakerTransformer;
import top.outlands.mod_of_mending.transformer.dropt.ValidatorAdapterFactoryTransformer;
import top.outlands.mod_of_mending.transformer.ears.EarsASMTransformer;
import top.outlands.mod_of_mending.transformer.enchantmentcontrol.EnumInputClassTransformer;
import top.outlands.mod_of_mending.transformer.endercore.EnderCoreTransformerTransformer;
import top.outlands.mod_of_mending.transformer.exu.FieldSetterTransformer;
import top.outlands.mod_of_mending.transformer.essential.EssentialSetupTweakerTransformer;
import top.outlands.mod_of_mending.transformer.essential.EssentialRelaunchTransformer;
import top.outlands.mod_of_mending.transformer.essential.EssentialGlobalMouseOverrideTransformer;
import top.outlands.mod_of_mending.transformer.essential.EssentialTransformerClearTransformer;
import top.outlands.mod_of_mending.transformer.essential.EssentialTelemetryManagerTransformer;
import top.outlands.mod_of_mending.transformer.farseek.FarseekClassTransformerTransformer;
import top.outlands.mod_of_mending.transformer.fivezig.ClassTweakerTransformer;
import top.outlands.mod_of_mending.transformer.forgeendertech.ForgeMainTransformer;
import top.outlands.mod_of_mending.transformer.futuremc.CoreTransformerTransformer;
import top.outlands.mod_of_mending.transformer.ic2ce.Ic2cExtrasLoadingPluginTransformer;
import top.outlands.mod_of_mending.transformer.integrated_proxy.MixinLoaderTransformer;
import top.outlands.mod_of_mending.transformer.invtweaks.InvTweaksHandlerShortcutsTransformer;
import top.outlands.mod_of_mending.transformer.journeymap.FileHandlerTransformer;
import top.outlands.mod_of_mending.transformer.journeymap.ThemeLoaderTransformer;
import top.outlands.mod_of_mending.transformer.journeymap.VersionCheck$1Transformer;
import top.outlands.mod_of_mending.transformer.kubejs.KubeJSTransformer;
import top.outlands.mod_of_mending.transformer.logisticpipes.*;
import top.outlands.mod_of_mending.transformer.light_and_shadow.AsmTransformerTransformer;
import top.outlands.mod_of_mending.transformer.loliasm.JavaFixesTransformer;
import top.outlands.mod_of_mending.transformer.loliasm.LoliFMLCallHookTransformer;
import top.outlands.mod_of_mending.transformer.loliasm.LoliReflectorTransformer;
import top.outlands.mod_of_mending.transformer.loliasm.ModIdentifierTransformer;
import top.outlands.mod_of_mending.transformer.mc4.TransformEntityLivingTransformer;
import top.outlands.mod_of_mending.transformer.mobends.ConnectionHelperTransformation;
import top.outlands.mod_of_mending.transformer.moreplayermodels.ClientEventHandlerTransformer;
import top.outlands.mod_of_mending.transformer.moresoundconfig.SoundDevicesTransformer;
import top.outlands.mod_of_mending.transformer.nbtperipheral.LinkedTreeMapTransformer;
import top.outlands.mod_of_mending.transformer.ocwasm.ByteBufferReferenceTransformer;
import top.outlands.mod_of_mending.transformer.offlineskins.ObfHelperTransformer;
import top.outlands.mod_of_mending.transformer.offlineskins.OfflineskinsTransformersTransformer;
import top.outlands.mod_of_mending.transformer.offlineskins.SetupTransformer;
import top.outlands.mod_of_mending.transformer.polyfrost.LaunchWrapperTweakerTransformer;
import top.outlands.mod_of_mending.transformer.randomtitle.ConfigManagerTransformer;
import top.outlands.mod_of_mending.transformer.rebind.EnumInputClassesTransformer;
import top.outlands.mod_of_mending.transformer.saoui.HudTransformer;
import top.outlands.mod_of_mending.transformer.screenshot_viewer.ScreenshotViewerTransformer;
import top.outlands.mod_of_mending.transformer.sereneseasons.ASMHelperTransformer;
import top.outlands.mod_of_mending.transformer.sereneseasons.EntityRendererTransformerTransformer;
import top.outlands.mod_of_mending.transformer.sevendaystomine.CommonProxyTransformer;
import top.outlands.mod_of_mending.transformer.shouldersurfing.EntityPlayerRayTraceTransformer;
import top.outlands.mod_of_mending.transformer.simplehotspring.SimplyHotSpringsConfigTransformer;
import top.outlands.mod_of_mending.transformer.smoothfont.FontRendererTransformer;
import top.outlands.mod_of_mending.transformer.splashanimation.SplashProgressTransformerTransformer;
import top.outlands.mod_of_mending.transformer.subaquatic.PluginBiomeTransformer;
import top.outlands.mod_of_mending.transformer.subaquatic.PluginEntityTransformer;
import top.outlands.mod_of_mending.transformer.subaquatic.SubaquaticIMTransformer;
import top.outlands.mod_of_mending.transformer.survivalinc.ForgeASMInjectorTransformer;
import top.outlands.mod_of_mending.transformer.techgun.TechgunsASMTransformerTransformer;
import top.outlands.mod_of_mending.transformer.transcend.FMLLoadingPluginTransformer;
import top.outlands.mod_of_mending.transformer.transcend.TweakerTransformer;
import top.outlands.mod_of_mending.transformer.universal.RemoveMixinInitFromCotrTransformer;
import top.outlands.mod_of_mending.transformer.tickcentral.*;
import top.outlands.mod_of_mending.transformer.universal.*;
import top.outlands.mod_of_mending.transformer.universal.ModifiersWriteRedirectTransformer;
import top.outlands.mod_of_mending.transformer.vintagefix.MixinConfigPluginTransformer;
import top.outlands.mod_of_mending.transformer.valkyrienskies.WorldPhysicsColliderTransformer;
import top.outlands.mod_of_mending.transformer.vampirism.TConstructBloodConversionTransformer;
import top.outlands.mod_of_mending.transformer.vampirism.WorldGenVampireOrchidTransformer;
import top.outlands.mod_of_mending.transformer.xnet.EnergyConnectorSettingsTransformer;
import top.outlands.mod_of_mending.transformer.zerocore.DisplayListTransformer;
import top.outlands.foundation.TransformerDelegate;
import top.outlands.foundation.boot.ActualClassLoader;

public class TransformerHelper {
    public static void registerTransformers() {

        if (ModOfMendingConfig.modPatchConfig.enableEnderCore) {
            TransformerDelegate.registerExplicitTransformer(
                    new EnderCoreTransformerTransformer(), "com.enderio.core.common.transform.EnderCoreTransformer");
        }
        if (ModOfMendingConfig.modPatchConfig.enableAR) {
            TransformerDelegate.registerExplicitTransformer(
                    new ClassTransformerTransformer(), "zmaster587.advancedRocketry.asm.ClassTransformer");
        }
        if (ModOfMendingConfig.modPatchConfig.enableShoulderSurfing) {
            TransformerDelegate.registerExplicitTransformer(
                    new EntityPlayerRayTraceTransformer(),
                    "com.teamderpy.shouldersurfing.asm.transformers.EntityPlayerRayTrace");
        }
        if (ModOfMendingConfig.modPatchConfig.enableSA) {
            TransformerDelegate.registerExplicitTransformer(
                    new SplashProgressTransformerTransformer(),
                    "pl.asie.splashanimation.core.SplashProgressTransformer");
        }
        if (ModOfMendingConfig.modPatchConfig.enableTickCentral) {
            TransformerDelegate.registerExplicitTransformer(
                    new InitializerTransformer(), "com.github.terminatornl.laggoggles.tickcentral.Initializer");
            TransformerDelegate.registerExplicitTransformer(
                    new ClassSnifferTransformer(), "com.github.terminatornl.tickcentral.api.ClassSniffer");
            TransformerDelegate.registerExplicitTransformer(
                    new CompatibilityTransformer(), "com.github.terminatornl.tickcentral.asm.Compatibility");
            TransformerDelegate.registerExplicitTransformer(
                    new TickCentralTransformer(), "com.github.terminatornl.tickcentral.TickCentral");
            TransformerDelegate.registerExplicitTransformer(
                    new PriorityAppendTransformer(),
                    "com.github.terminatornl.tickcentral.asm.BlockTransformer",
                    "com.github.terminatornl.tickcentral.asm.ITickableTransformer",
                    "com.github.terminatornl.tickcentral.asm.EntityTransformer",
                    "com.github.terminatornl.tickcentral.asm.HubAPITransformer",
                    "net.minecraftforge.fml.common.asm.transformers.ModAPITransformer");
        }
        if (ModOfMendingConfig.modPatchConfig.enableLP) {
            TransformerDelegate.registerExplicitTransformer(
                    new LogisticPipesHandlerTransformer(),
                    "logisticspipes.asm.mcmp.ClassBlockMultipartContainerHandler",
                    "logisticspipes.asm.td.ClassRenderDuctItemsHandler",
                    "logisticspipes.asm.td.ClassTravelingItemHandler");
            TransformerDelegate.registerExplicitTransformer(
                    new LogisticsClassTransformerTransformer(ActualClassLoader.class),
                    "logisticspipes.asm.LogisticsClassTransformer",
                    "logisticspipes.asm.LogisticsPipesClassInjector");
            TransformerDelegate.registerExplicitTransformer(
                    new LogisticsPipesClassInjectorTransformer(), "logisticspipes.asm.LogisticsPipesClassInjector");
            TransformerDelegate.registerExplicitTransformer(
                    new defineClassTransformer(),
                    "logisticspipes.asm.wrapper.LogisticsWrapperHandler",
                    "logisticspipes.proxy.opencomputers.asm.ClassCreator");
            TransformerDelegate.registerExplicitTransformer(
                    new LoadClassTransformer(),
                    "network.rs485.debug.OpenGLDebugger",
                    "logisticspipes.utils.StaticResolverUtil");
            TransformerDelegate.registerExplicitTransformer(
                    new StaticResolverUtilTransformer(), "logisticspipes.utils.StaticResolverUtil");
        }
        if (ModOfMendingConfig.modPatchConfig.enableEC) {
            TransformerDelegate.registerExplicitTransformer(
                    new EnumInputClassTransformer(), "austeretony.enchcontrol.common.core.EnumInputClass");
        }
        if (ModOfMendingConfig.modPatchConfig.enableTheASM) {
            TransformerDelegate.registerExplicitTransformer(
                    new LoliReflectorTransformer(), "zone.rong.loliasm.LoliReflector");
            TransformerDelegate.registerExplicitTransformer(
                    new JavaFixesTransformer(), "zone.rong.loliasm.common.java.JavaFixes");
            TransformerDelegate.registerExplicitTransformer(
                    new LoliFMLCallHookTransformer(), "zone.rong.loliasm.core.LoliFMLCallHook");
            TransformerDelegate.registerExplicitTransformer(
                    new ModIdentifierTransformer(), "zone.rong.loliasm.common.crashes.ModIdentifier");
        }
        if (ModOfMendingConfig.modPatchConfig.enableZeroCore) {
            TransformerDelegate.registerExplicitTransformer(
                    new DisplayListTransformer(), "it.zerono.mods.zerocore.lib.client.render.DisplayList");
        }
        if (ModOfMendingConfig.modPatchConfig.enableSmoothFont) {
            TransformerDelegate.registerExplicitTransformer(
                    new FontRendererTransformer(), "net.minecraft.client.gui.FontRenderer");
        }
        if (ModOfMendingConfig.modPatchConfig.enableCSL) {
            TransformerDelegate.registerExplicitTransformer(
                    new ForgeTweakerTransformer(), "customskinloader.forge.ForgeTweaker");
        }
        if (ModOfMendingConfig.modPatchConfig.enableXNet) {
            TransformerDelegate.registerExplicitTransformer(
                    new EnergyConnectorSettingsTransformer(), "mcjty.xnet.apiimpl.energy.EnergyConnectorSettings");
        }
        if (ModOfMendingConfig.modPatchConfig.enableSurvivialInc) {
            TransformerDelegate.registerExplicitTransformer(
                    new ForgeASMInjectorTransformer(), "enginecrafter77.survivalinc.util.ForgeASMInjector");
        }
        if (ModOfMendingConfig.modPatchConfig.enableSubaquatic) {
            TransformerDelegate.registerExplicitTransformer(
                    new PluginEntityTransformer(),
                    "git.jbredwards.subaquatic.mod.asm.plugin.vanilla.entity.PluginEntity");
            TransformerDelegate.registerExplicitTransformer(
                    new SubaquaticIMTransformer(), "net.minecraft.world.gen.layer.GenLayer");
            TransformerDelegate.registerExplicitTransformer(
                    new PluginBiomeTransformer(), "git.jbredwards.subaquatic.mod.asm.plugin.vanilla.world.PluginBiome");
        }
        if (ModOfMendingConfig.modPatchConfig.enableIC2CE) {
            TransformerDelegate.registerExplicitTransformer(
                    new Ic2cExtrasLoadingPluginTransformer(), "trinsdar.ic2c_extras.asm.Ic2cExtrasLoadingPlugin");
        }
        if (ModOfMendingConfig.modPatchConfig.enableSimplyHotSpring) {
            TransformerDelegate.registerExplicitTransformer(
                    new SimplyHotSpringsConfigTransformer(),
                    "connor135246.simplyhotsprings.util.SimplyHotSpringsConfig");
        }
        if (ModOfMendingConfig.modPatchConfig.enableVampirism) {
            TransformerDelegate.registerExplicitTransformer(
                    new WorldGenVampireOrchidTransformer(),
                    "de.teamlapen.vampirism.biome.BiomeGenVampireForest$WorldGenVampireOrchid");
            TransformerDelegate.registerExplicitTransformer(
                    new TConstructBloodConversionTransformer(),
                    "de.teamlapen.vampirism_integrations.tconstruct.TConstructBloodConversion");
        }
        if (ModOfMendingConfig.modPatchConfig.enableExtraUtilities) {
            TransformerDelegate.registerExplicitTransformer(
                    new FieldSetterTransformer(), "com.rwtema.extrautils2.utils.datastructures.FieldSetter");
        }
        if (ModOfMendingConfig.modPatchConfig.enableBetterFC) {
            TransformerDelegate.registerExplicitTransformer(
                    new HK_LoaderTransformer(), "kpan.better_fc.asm.hook.HK_Loader");
        }
        if (ModOfMendingConfig.modPatchConfig.enable5zig) {
            TransformerDelegate.registerExplicitTransformer(
                    new ClassTweakerTransformer(), "eu.the5zig.mod.asm.ClassTweaker");
        }
        if (ModOfMendingConfig.modPatchConfig.enableEars) {
            TransformerDelegate.registerExplicitTransformer(
                    new EarsASMTransformer(),
                    "com.unascribed.ears.asm.ImageBufferDownloadTransformer",
                    "com.unascribed.ears.asm.LayerElytraTransformer",
                    "com.unascribed.ears.asm.RenderPlayerTransformer",
                    "com.unascribed.ears.asm.ThreadDownloadImageDataTransformer",
                    "com.unascribed.ears.common.agent.mini.MiniTransformer",
                    "com.unascribed.ears.common.agent.mini.PatchContext",
                    "com.unascribed.ears.common.agent.mini.PatchContext$SearchResult");
        }
        if (ModOfMendingConfig.modPatchConfig.enableColytra) {
            TransformerDelegate.registerExplicitTransformer(
                    new EntityLivingBaseTransformer(), "net.minecraft.entity.EntityLivingBase");
        }
        if (ModOfMendingConfig.modPatchConfig.enableCrossbow) {
            TransformerDelegate.registerExplicitTransformer(
                    new TransformerEntityArrowTransformer(),
                    "git.jbredwards.crossbow.mod.asm.transformer.TransformerEntityArrow");
        }
        if (ModOfMendingConfig.modPatchConfig.enablePolyForst) {
            TransformerDelegate.registerExplicitTransformer(
                    new LaunchWrapperTweakerTransformer(),
                    "cc.polyfrost.oneconfig.loader.stage0.LaunchWrapperTweaker",
                    "cc.polyfrost.oneconfig.loader.OneConfigLoader");
        }
        if (ModOfMendingConfig.modPatchConfig.enableDropt) {
            TransformerDelegate.registerExplicitTransformer(
                    new ValidatorAdapterFactoryTransformer(),
                    "com.codetaylor.mc.dropt.modules.dropt.rule.RuleLoader$ValidatorAdapterFactory");
        }
        if (ModOfMendingConfig.modPatchConfig.enableIntegratedProxyPatch) {
            TransformerDelegate.registerExplicitTransformer(
                    new MixinLoaderTransformer(), "com.shblock.integrated_proxy.mixin.MixinLoader");
        }
        RemoveMixinInitFromCotrTransformer instance = new RemoveMixinInitFromCotrTransformer();
        if (ModOfMendingConfig.modPatchConfig.enableThaumicFixesPatch) {
            TransformerDelegate.registerExplicitTransformer(
                    instance, "com.seriouscreeper.thaumicfixes.core.ThaumicFixesLoadingPlugin");
        }
        if (ModOfMendingConfig.modPatchConfig.enableErebusFixPatch) {
            TransformerDelegate.registerExplicitTransformer(instance, "noobanidus.mods.erebusfix.core.EFLoadingPlugin");
        }
        if (ModOfMendingConfig.modPatchConfig.enableUncraftingBlacklist) {
            TransformerDelegate.registerExplicitTransformer(
                    instance, "doomanidus.mods.uncraftingblacklist.core.UBLoadingPlugin");
        }
        if (ModOfMendingConfig.modPatchConfig.enableEssential) {
            TransformerDelegate.registerExplicitTransformer(
                    new EssentialSetupTweakerTransformer(),
                    "gg.essential.loader.stage0.EssentialSetupTweaker",
                    "gg.essential.loader.stage1.EssentialLoader",
                    "gg.essential.loader.stage2.EssentialLoader",
                    "gg.essential.main.Bootstrap");
            TransformerDelegate.registerExplicitTransformer(
                    new EssentialRelaunchTransformer(), "gg.essential.loader.stage2.relaunch.Relaunch");
            TransformerDelegate.registerExplicitTransformer(
                    new EssentialGlobalMouseOverrideTransformer(),
                    "gg.essential.gui.overlay.OverlayManagerImpl$GlobalMouseOverride");
            TransformerDelegate.registerExplicitTransformer(
                    new EssentialTransformerClearTransformer(),
                    "gg.essential.asm.compat.betterfps.BetterFpsTransformerWrapper",
                    "gg.essential.asm.compat.PhosphorTransformer");
            TransformerDelegate.registerExplicitTransformer(
                    new EssentialTelemetryManagerTransformer(),
                    "gg.essential.network.connectionmanager.telemetry.TelemetryManager");
        }
        if (ModOfMendingConfig.modPatchConfig.enableLightAndShadow) {
            TransformerDelegate.registerExplicitTransformer(
                    new AsmTransformerTransformer(), "kpan.light_and_shadow.asm.core.AsmTransformer");
        }
        if (ModOfMendingConfig.modPatchConfig.enableJourneyMap) {
            TransformerDelegate.registerExplicitTransformer(
                    new ThemeLoaderTransformer(), "journeymap.client.io.ThemeLoader");
            TransformerDelegate.registerExplicitTransformer(
                    new FileHandlerTransformer(), "journeymap.client.io.FileHandler");
            TransformerDelegate.registerExplicitTransformer(
                    new VersionCheck$1Transformer(), "journeymap.common.version.VersionCheck$1");
        }
        if (ModOfMendingConfig.modPatchConfig.enableOfflineSkins) {
            TransformerDelegate.registerExplicitTransformer(
                    new ObfHelperTransformer(), "lain.mods.skins.init.forge.asm.ObfHelper");
            TransformerDelegate.registerExplicitTransformer(
                    new OfflineskinsTransformersTransformer(), "lain.mods.skins.init.forge.asm.ASMTransformer");
            TransformerDelegate.registerExplicitTransformer(
                    new SetupTransformer(), "lain.mods.skins.init.forge.asm.Setup");
        }
        if (ModOfMendingConfig.modPatchConfig.enableTechgun) {
            TransformerDelegate.registerExplicitTransformer(
                    new TechgunsASMTransformerTransformer(), "techguns.core.TechgunsASMTransformer");
        }
        if (ModOfMendingConfig.modPatchConfig.enableCorpse) {
            TransformerDelegate.registerExplicitTransformer(
                    new MessageOpenHistoryTransformer(), "de.maxhenkel.corpse.net.MessageOpenHistory");
        }
        if (ModOfMendingConfig.modPatchConfig.enableScreenshotViewer) {
            TransformerDelegate.registerExplicitTransformer(
                    new ScreenshotViewerTransformer(),
                    "io.github.lgatodu47.screenshot_viewer.ScreenshotViewer$ScreenshotViewerEvents");
        }
        if (ModOfMendingConfig.modPatchConfig.enableInvTweaks) {
            TransformerDelegate.registerExplicitTransformer(
                    new InvTweaksHandlerShortcutsTransformer(), "invtweaks.InvTweaksHandlerShortcuts");
        }
        if (ModOfMendingConfig.modPatchConfig.enableMoreSoundConfig) {
            TransformerDelegate.registerExplicitTransformer(
                    new SoundDevicesTransformer(), "ichttt.mods.moresoundconfig.SoundDevices");
        }
        if (ModOfMendingConfig.modPatchConfig.enableNBTPeripheral) {
            TransformerDelegate.registerExplicitTransformer(
                    new LinkedTreeMapTransformer(),
                    "nl.makertim.nbtperipheral.cc.BlockStateUtil",
                    "nl.makertim.nbtperipheral.cc.CCUtil",
                    "nl.makertim.nbtperipheral.cc.NBTUtil");
        }
        if (ModOfMendingConfig.modPatchConfig.enableFarseek) {
            TransformerDelegate.registerExplicitTransformer(
                    new FarseekClassTransformerTransformer(), "farseek.core.FarseekClassTransformer");
        }
        if (ModOfMendingConfig.modPatchConfig.enableKubeJS) {
            TransformerDelegate.registerExplicitTransformer(
                    new RemapTransformer(
                            new String[] {"jdk/nashorn/api/scripting/"},
                            new String[] {"org/openjdk/nashorn/api/scripting/"}),
                    "dev.latvian.kubejs.event.EventsJS",
                    "dev.latvian.kubejs.fluid.FluidStackJS",
                    "dev.latvian.kubejs.item.ingredient.IngredientJS",
                    "dev.latvian.kubejs.item.ItemStackJS",
                    "dev.latvian.kubejs.script.ScriptManager",
                    "dev.latvian.kubejs.server.KubeJSServerEventHandler",
                    "dev.latvian.kubejs.util.FunctionBinding",
                    "dev.latvian.kubejs.util.JsonUtilsJS",
                    "dev.latvian.kubejs.util.nbt.NBTBaseJS",
                    "dev.latvian.kubejs.util.UtilsJS");
            TransformerDelegate.registerExplicitTransformer(new KubeJSTransformer(), "dev.latvian.kubejs.KubeJS");
        }
        if (ModOfMendingConfig.modPatchConfig.enableSaoUI) {
            TransformerDelegate.registerExplicitTransformer(
                    new HudTransformer(), "com.tencao.saoui.themes.elements.Hud");
            TransformerDelegate.registerExplicitTransformer(
                    new RemapTransformer(new String[] {"javax/xml/bind/"}, new String[] {"jakarta/xml/bind/"}),
                    "com.tencao.saoui.SAOCore",
                    "com.tencao.saoui.themes.ThemeLoader",
                    "com.tencao.saoui.themes.elements.Element",
                    "com.tencao.saoui.themes.elements.ElementGroup",
                    "com.tencao.saoui.themes.elements.GLHotbarItem",
                    "com.tencao.saoui.themes.elements.GLRectangle",
                    "com.tencao.saoui.themes.elements.GLString",
                    "com.tencao.saoui.themes.elements.Hud",
                    "com.tencao.saoui.themes.elements.package-info",
                    "com.tencao.saoui.themes.elements.RawElement",
                    "com.tencao.saoui.themes.elements.RepetitionGroup",
                    "com.tencao.saoui.themes.util.CBoolean",
                    "com.tencao.saoui.themes.util.CDouble",
                    "com.tencao.saoui.themes.util.CInt",
                    "com.tencao.saoui.themes.util.CString",
                    "com.tencao.saoui.themes.util.CUnit",
                    "com.tencao.saoui.themes.util.ExpressionAdapter",
                    "com.tencao.saoui.themes.util.ExpressionIntermediate");
        }
        if (ModOfMendingConfig.modPatchConfig.enableForgeEndertech) {
            TransformerDelegate.registerExplicitTransformer(
                    new ForgeMainTransformer(), "com.endertech.minecraft.forge.ForgeMain");
        }
        if (ModOfMendingConfig.modPatchConfig.enableMorePlayerModel) {
            TransformerDelegate.registerExplicitTransformer(
                    new ClientEventHandlerTransformer(), "noppes.mpm.client.ClientEventHandler");
        }
        if (ModOfMendingConfig.modPatchConfig.enableUnvalkyriedHeavens) {
            TransformerDelegate.registerExplicitTransformer(
                    new WorldPhysicsColliderTransformer(),
                    "org.valkyrienskies.mod.common.collision.WorldPhysicsCollider");
        }
        if (ModOfMendingConfig.modPatchConfig.enableMoBends) {
            TransformerDelegate.registerExplicitTransformer(
                    new ConnectionHelperTransformation(), "goblinbob.mobends.core.util.ConnectionHelper");
            TransformerDelegate.registerExplicitTransformer(
                    new RemapTransformer(
                            new String[] {"org/apache/http/conn/"}, new String[] {"org/apache/hc/client5/http/"}),
                    "goblinbob.mobends.core.connection.PlayerSettingsDownloader",
                    "goblinbob.mobends.core.asset.AssetsModule",
                    "goblinbob.mobends.core.supporters.SupporterContent");
            TransformerDelegate.registerExplicitTransformer(
                    new RemapTransformer(
                            new String[] {"org/apache/http/util/"}, new String[] {"org/apache/hc/core5/util/"}),
                    "goblinbob.mobends.core.util.SerialHelper",
                    "goblinbob.mobends.core.animation.keyframe.BinaryAnimationLoader");
        }
        if (ModOfMendingConfig.modPatchConfig.enableAetherII) {
            TransformerDelegate.registerExplicitTransformer(
                    new ClientProxyTransformer(), "com.gildedgames.aether.client.ClientProxy");
        }
        if (ModOfMendingConfig.modPatchConfig.enableRandomTitle) {
            TransformerDelegate.registerExplicitTransformer(
                    new ConfigManagerTransformer(), "me.PercyDan.RandomTitle.ConfigManager");
        }
        if (ModOfMendingConfig.modPatchConfig.enableAllMusic) {
            TransformerDelegate.registerExplicitTransformer(
                    new AllMusicHudTransformer(), "com.coloryr.allmusic.client.core.hud.AllMusicHud");

            TransformerDelegate.registerExplicitTransformer(
                    new AllMusicPlayerTransformer(), "com.coloryr.allmusic.client.core.player.AllMusicPlayer");
        }
        if (ModOfMendingConfig.modPatchConfig.enableSereneSeasons) {
            TransformerDelegate.registerExplicitTransformer(
                    new EntityRendererTransformerTransformer(),
                    "sereneseasons.asm.transformer.EntityRendererTransformer");
            TransformerDelegate.registerExplicitTransformer(new ASMHelperTransformer(), "sereneseasons.asm.ASMHelper");
        }
        if (ModOfMendingConfig.modPatchConfig.enableRebind) {
            TransformerDelegate.registerExplicitTransformer(
                    new EnumInputClassesTransformer(), "austeretony.rebind.common.core.EnumInputClasses");
        }
        if (ModOfMendingConfig.modPatchConfig.enableFutureMC) {
            TransformerDelegate.registerExplicitTransformer(
                    new CoreTransformerTransformer(), "thedarkcolour.futuremc.asm.CoreTransformer");
        }
        /*
        TransformerDelegate.registerExplicitTransformer(
                new PorkUtilTransformer(),
                "net.buildtheearth.terraplusplus.dep.net.daporkchop.lib.common.util.PorkUtil");*/
        if (ModOfMendingConfig.modPatchConfig.enableSevenDaysToMine) {
            TransformerDelegate.registerExplicitTransformer(
                    new CommonProxyTransformer(), "nuparu.sevendaystomine.proxy.CommonProxy");
        }
        if (ModOfMendingConfig.modPatchConfig.enableBetterSprinting) {
            TransformerDelegate.registerExplicitTransformer(
                    new TransformerEntityPlayerSPTransformer(),
                    "chylex.bettersprinting.system.core.TransformerEntityPlayerSP");
        }
        if (ModOfMendingConfig.modPatchConfig.enableHeavyFalling) {
            TransformerDelegate.registerExplicitTransformer(
                    new top.outlands.mod_of_mending.transformer.heavy_fallings.AsmTransformerTransformer(),
                    "kpan.heavy_fallings.asm.core.AsmTransformer");
        }
        if (ModOfMendingConfig.modPatchConfig.enableMetroCubed4) {
            TransformerDelegate.registerExplicitTransformer(
                    new TransformEntityLivingTransformer(),
                    "firemerald.mc4.plugin.transformers.common.TransformEntityLiving");
        }
        if (ModOfMendingConfig.modPatchConfig.enableChiseledMe) {
            TransformerDelegate.registerExplicitTransformer(
                    new ChiseledMeLoaderTransformer(), "net.minecraftforge.fml.common.Loader");
        }
        if (ModOfMendingConfig.modPatchConfig.enableOCWASM) {
            TransformerDelegate.registerExplicitTransformer(
                    new ByteBufferReferenceTransformer(),
                    "asmble.compile.jvm.ByteBufferMem",
                    "asmble.compile.jvm.ByteBufferMem$init$1",
                    "asmble.compile.jvm.ByteBufferMem$currentMemory$1",
                    "asmble.compile.jvm.ByteBufferMem$getOrCreateGrowMemoryMethod$3$node$1",
                    "asmble.compile.jvm.ByteBufferMem$getOrCreateGrowMemoryMethod$3$node$3",
                    "asmble.compile.jvm.ByteBufferMem$data$2");
        }
        if (ModOfMendingConfig.modPatchConfig.enableTranscend) {
            TransformerDelegate.registerExplicitTransformer(
                    new FMLLoadingPluginTransformer(), "huige233.transcend.asm.FMLLoadingPlugin");
            TransformerDelegate.registerExplicitTransformer(
                    new TweakerTransformer(), "huige233.transcend.asm.tweaker.Tweaker");
        }
        if (ModOfMendingConfig.modPatchConfig.enableVintageFixPatch) {
            TransformerDelegate.registerExplicitTransformer(
                    new MixinConfigPluginTransformer(), "org.embeddedt.vintagefix.core.MixinConfigPlugin");
        }

        // ======================================== Common patches below ===============================================

        
        if (ModOfMendingConfig.getCodeSourcePatchTargets.length > 0) {
            TransformerDelegate.registerExplicitTransformer(
                    new ITweakerTransformer(), ModOfMendingConfig.getCodeSourcePatchTargets);
        }
        if (!ModOfMendingConfig.modifiersWriteRedirectTargets.isEmpty()) {
                TransformerDelegate.registerExplicitTransformer(
                        new ModifiersWriteRedirectTransformer(ModOfMendingConfig.modifiersWriteRedirectTargets),
                    ModOfMendingConfig.modifiersWriteRedirectTargets.keySet().toArray(new String[0]));
            
        }
        if (ModOfMendingConfig.reflectionPatchTargets.length > 0) {
            TransformerDelegate.registerExplicitTransformer(
                    new ReflectFieldTransformer(), ModOfMendingConfig.reflectionPatchTargets);
        }
        if (ModOfMendingConfig.getURLPatchTargets.length > 0) {
            TransformerDelegate.registerExplicitTransformer(
                    new URLClassLoaderTransformer(), ModOfMendingConfig.getURLPatchTargets);
        }
        if (ModOfMendingConfig.scriptEngineTargets.length > 0) {
            TransformerDelegate.registerExplicitTransformer(
                    new ScriptEngineTransformer(), ModOfMendingConfig.scriptEngineTargets);
        }
        if (ModOfMendingConfig.UUIDTargets.length > 0) {
            TransformerDelegate.registerExplicitTransformer(new MalformedUUIDTransformer(), ModOfMendingConfig.UUIDTargets);
        }
        if (ModOfMendingConfig.remapTargets.length > 0) {
            TransformerDelegate.registerExplicitTransformer(
                    new RemapTransformer(
                            new String[] {
                                "javax/xml/bind/",
                                "javax/xml/ws/",
                                "javax/ws/",
                                "javax/activation/",
                                "javax/soap/",
                                "javax/jws/",
                                "javax/inject/"
                            },
                            new String[] {
                                "jakarta/xml/bind/",
                                "jakarta/xml/ws/",
                                "jakarta/ws/",
                                "jakarta/activation/",
                                "jakarta/soap/",
                                "jakarta/jws/",
                                "jakarta/inject/"
                            }),
                    ModOfMendingConfig.remapTargets);
        }
        if (ModOfMendingConfig.remapLWTargets.length > 0) {
            TransformerDelegate.registerExplicitTransformer(new RemapLegacyLWTransformer(), ModOfMendingConfig.remapLWTargets);
        }
        if (ModOfMendingConfig.remapOshiTargets.length > 0) {
            TransformerDelegate.registerExplicitTransformer(
                    new RemapTransformer(
                            new String[] {
                                "oshi/SystemInfo",
                            },
                            new String[] {
                                "oshi/ffm/SystemInfo",
                            }),
                    ModOfMendingConfig.remapOshiTargets);
        }
        if (ModOfMendingConfig.remapReflectionTargets.length > 0) {
            TransformerDelegate.registerExplicitTransformer(
                    new RemapSunReflectionTransformer(), ModOfMendingConfig.remapReflectionTargets);
        }
        if (ModOfMendingConfig.computeIfAbsentTargets.length > 0) {
            TransformerDelegate.registerExplicitTransformer(
                    new ComputeIfAbsentTransformer(), ModOfMendingConfig.computeIfAbsentTargets);
        }
        if (ModOfMendingConfig.addFutureCallbackTargets.length > 0) {
            TransformerDelegate.registerExplicitTransformer(
                    new AddFutureCallbackTransformer(), ModOfMendingConfig.addFutureCallbackTargets);
        }
        if (ModOfMendingConfig.mouseWheelPatchTargets.length > 0) {
            TransformerDelegate.registerExplicitTransformer(
                    new DWheelTransformer(), ModOfMendingConfig.mouseWheelPatchTargets);
        }
        if (ModOfMendingConfig.toArrayPatchTargets.length > 0) {
            TransformerDelegate.registerExplicitTransformer(new ToArrayTransformer(), ModOfMendingConfig.toArrayPatchTargets);
        }
        if (ModOfMendingConfig.nonUpdateTargets.length > 0) {
            TransformerDelegate.registerExplicitTransformer(
                new ConnectionBlockingTransformer(), ModOfMendingConfig.nonUpdateTargets);
        }
        if (!ModOfMendingConfig.finalRemovingTargets.isEmpty()) {
            TransformerDelegate.registerExplicitTransformer(
                new FinalStripperTransformer(ModOfMendingConfig.finalRemovingTargets),
                ModOfMendingConfig.finalRemovingTargets.keySet().toArray(new String[0]));
        }
    }
}
