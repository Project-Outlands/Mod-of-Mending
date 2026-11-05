package top.outlands.mod_of_mending.mixin.hammercore;

import top.outlands.mod_of_mending.common.ModOfMending;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import top.outlands.mod_of_mending.helper.FieldAccess;
import com.zeitheron.hammercore.utils.ReflectionUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

@Mixin(value = ReflectionUtil.class, remap = false)
public class ReflectionUtilMixin {
    @Inject(method = "setStaticFinalField(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Object;)Z", at = @At("HEAD"), cancellable = true)
    private static void setFinalFieldStaticInClass(Class<?> cls, String _var, Object val, CallbackInfoReturnable<Boolean> cir) {
        try {
            FieldAccess.setField(cls.getDeclaredField(_var), null, val);
        } catch (NoSuchFieldException e) {
            ModOfMending.LOGGER.error(e);
        }
        cir.setReturnValue(true);
    }
    
    @Inject(method = "setStaticFinalField(Ljava/lang/reflect/Field;Ljava/lang/Object;)Z", at = @At("HEAD"), cancellable = true)
    private static void setFinalFieldStatic(Field f, Object val, CallbackInfoReturnable<Boolean> cir) {
        FieldAccess.setField(f, null, val);
        cir.setReturnValue(true);
    }
    
    @Inject(method = "setFinalField", at = @At("HEAD"), cancellable = true)
    private static void setFinalField(Field f, Object instance, Object thing, CallbackInfoReturnable<Boolean> cir) {
        FieldAccess.setField(f, instance, thing);
        cir.setReturnValue(true);
    }
    
    @Inject(method = "makeWritable", at = @At("HEAD"), cancellable = true)
    private static void makeWritable(Field f, CallbackInfoReturnable<Field> cir) {
        try {
            ObfuscationReflectionHelper.removeFinalModifier(f);
        } catch (IllegalAccessException e) {
            ModOfMending.LOGGER.error(e);
        }
        cir.setReturnValue(f);
    }
}
