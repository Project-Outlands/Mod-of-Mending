package top.outlands.mod_of_mending.mixin.glibysphysics;

import gliby.minecraft.gman.GMan;
import gliby.minecraft.gman.ModInfo;
import net.minecraftforge.fml.common.ModContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = GMan.class, remap = false)
public abstract class GManMixin {
    /**
     * @author ModOfMending
     * @reason Useless version check, mod is dead
     */
    @Overwrite
    public static void addCheckResult(ModInfo modInfo, ModContainer container) {
    }
}
