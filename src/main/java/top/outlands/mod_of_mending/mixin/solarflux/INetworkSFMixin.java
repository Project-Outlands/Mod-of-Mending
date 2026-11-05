package top.outlands.mod_of_mending.mixin.solarflux;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import tk.zeitheron.solarflux.net.NetworkSF;

@Mixin(value = NetworkSF.class, remap = false)
public interface INetworkSFMixin {
    
    @Accessor
    @Mutable
    public static void setINSTANCE(NetworkSF instance){}
}
