package top.outlands.mod_of_mending.mixin.darktheme;

import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import top.outlands.mod_of_mending.helper.FieldAccess;
import net.minecraftforge.fml.common.FMLLog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;

import java.lang.reflect.Field;

@SuppressWarnings("OverwriteAuthorRequired")
@Pseudo
@Mixin(targets = "org.zeith.darktheme.internal.FinalFieldHelper")
public abstract class FinalFieldHelperMixin {

    @Overwrite(remap = false)
    public static boolean setStaticFinalField(Field f, Object val) {
        try {
            FieldAccess.setField(f, null, val);
            return true;
        } catch (Exception e) {
            FMLLog.log.warn("Failed to set static final field: " + f, e);
            return false;
        }
    }

    @Overwrite(remap = false)
    public static boolean setStaticFinalField(Class<?> clazz, String fieldName, Object val) {
        try {
            Field f = clazz.getDeclaredField(fieldName);
            FieldAccess.setField(f, null, val);
            return true;
        } catch (Exception e) {
            FMLLog.log.warn("Failed to set static final field: " + fieldName + " in " + clazz.getName(), e);
            return false;
        }
    }

    @Overwrite(remap = false)
    public static boolean setFinalField(Field f, Object instance, Object value) {
        try {
            FieldAccess.setField(f, instance, value);
            return true;
        } catch (Exception e) {
            FMLLog.log.warn("Failed to set final field: " + f, e);
            return false;
        }
    }

    @Overwrite(remap = false)
    public static Field makeWritable(Field f) {
        try {
            ObfuscationReflectionHelper.removeFinalModifier(f);
            return f;
        } catch (Exception e) {
            FMLLog.log.warn("Failed to make field writable: " + f, e);
            return f;
        }
    }
}
