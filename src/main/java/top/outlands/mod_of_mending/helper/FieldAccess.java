package top.outlands.mod_of_mending.helper;

import net.lenni0451.reflect.Classes;
import net.lenni0451.reflect.Fields;

import java.lang.reflect.Field;

/**
 * Reflective field access that survives a {@code final} modifier.
 *
 * <p>{@link Field#set} refuses a {@code final} target on modern JDKs, so mods that write one - and this mod's
 * transformer rewrites exactly those call sites - used to be routed through the loader's
 * {@code net.minecraftforge.hackery.ReflectionHackery}. That class is gone as of Outlands 2026.11.05 (it was kept
 * only for this remap target, see {@code bf7259f3}), so the same calls land here instead.
 *
 * <p>This is an adapter, not a reimplementation: the writes go through {@link Fields}, which is the upstream final
 * call the loader itself now uses for the same problem (see {@code ObjectHolderRef}). The reason it exists at all is
 * argument order - the transformer rewrites {@code Field.set(Object, Object)} with the receiver already on the stack,
 * so the emitted signature has to be {@code (Field, Object, value)}, while {@link Fields} takes
 * {@code (Object owner, Field, value)}. Swapping that on the stack would need a size-aware shuffle (long/double take
 * two slots), so the order is fixed here in ordinary Java instead.
 *
 * <p>This class is under the loader's transformer exclusions, so nothing in here is rewritten back into itself.
 */
public final class FieldAccess {

    private FieldAccess() { }

    public static void setField(Field field, Object owner, Object value) {
        init(field);
        Fields.set(owner, field, value);
    }

    public static Object getField(Field field, Object owner) {
        init(field);
        return Fields.get(owner, field);
    }

    public static void setBooleanField(Field field, Object owner, boolean value) {
        init(field);
        Fields.setBoolean(owner, field, value);
    }

    public static boolean getBooleanField(Field field, Object owner) {
        init(field);
        return Fields.getBoolean(owner, field);
    }

    public static void setByteField(Field field, Object owner, byte value) {
        init(field);
        Fields.setByte(owner, field, value);
    }

    public static byte getByteField(Field field, Object owner) {
        init(field);
        return Fields.getByte(owner, field);
    }

    public static void setCharField(Field field, Object owner, char value) {
        init(field);
        Fields.setChar(owner, field, value);
    }

    public static char getCharField(Field field, Object owner) {
        init(field);
        return Fields.getChar(owner, field);
    }

    public static void setShortField(Field field, Object owner, short value) {
        init(field);
        Fields.setShort(owner, field, value);
    }

    public static short getShortField(Field field, Object owner) {
        init(field);
        return Fields.getShort(owner, field);
    }

    public static void setIntField(Field field, Object owner, int value) {
        init(field);
        Fields.setInt(owner, field, value);
    }

    public static int getIntField(Field field, Object owner) {
        init(field);
        return Fields.getInt(owner, field);
    }

    public static void setLongField(Field field, Object owner, long value) {
        init(field);
        Fields.setLong(owner, field, value);
    }

    public static long getLongField(Field field, Object owner) {
        init(field);
        return Fields.getLong(owner, field);
    }

    public static void setFloatField(Field field, Object owner, float value) {
        init(field);
        Fields.setFloat(owner, field, value);
    }

    public static float getFloatField(Field field, Object owner) {
        init(field);
        return Fields.getFloat(owner, field);
    }

    public static void setDoubleField(Field field, Object owner, double value) {
        init(field);
        Fields.setDouble(owner, field, value);
    }

    public static double getDoubleField(Field field, Object owner) {
        init(field);
        return Fields.getDouble(owner, field);
    }

    /**
     * Runs the declaring class' {@code <clinit>} before a write reaches its fields.
     *
     * <p>A static field written through Unsafe before {@code <clinit>} runs is silently overwritten when it finally
     * does, so the write would appear to succeed and then be gone. The loader's own {@code ObjectHolderRef} has the
     * same note on the same call.
     */
    private static void init(Field field) {
        Classes.ensureInitialized(field.getDeclaringClass());
    }

}
