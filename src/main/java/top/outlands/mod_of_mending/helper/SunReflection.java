package top.outlands.mod_of_mending.helper;

import net.lenni0451.reflect.Classes;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Stand-in for {@code sun.reflect.Reflection}, which no JDK since 9 has shipped.
 *
 * <p>Mods compiled against Java 8 call it for "which class called me" (and, rarely, for member-access checks). The
 * loader used to ship this as {@code net.minecraftforge.hackery.Reflection}; Outlands 2026.11.05 deleted it (it was
 * kept only as a remap target), so it lives here.
 *
 * <p>The two caller lookups are not reimplemented - they delegate to {@link Classes#getCallerClass(int)}, which is the
 * upstream final call for the same question. The depth is passed as {@code depth + 1} because that call counts the frame
 * it was invoked from, and here that frame is this class rather than the mod code that asked. Everything else is a stub
 * returning "no opinion", which is what the loader's copy did: these entry points only ever existed to satisfy a
 * linkage error, and a mod that genuinely needs a real access check is on a path no modern JDK can run anyway.
 */
public final class SunReflection {

    private SunReflection() { }

    /**
     * {@return the class whose method called this one}
     *
     * <p>Same answer as {@code getCallerClass(0)}, which is what the loader's copy did - but it cannot reach it by
     * delegating to the overload: that would push this class' own frame in between, and the lookup would resolve to
     * {@code SunReflection} rather than to the caller.
     */
    public static Class<?> getCallerClass() {
        return Classes.getCallerClass(1);
    }

    public static Class<?> getCallerClass(int depth) {
        return Classes.getCallerClass(depth + 1);
    }

    public static int getClassAccessFlags(Class<?> clazz) {
        return clazz.getModifiers();
    }

    public static boolean quickCheckMemberAccess(Class<?> clazz, int modifiers) {
        return false;
    }

    public static void ensureMemberAccess(Class<?> caller, Class<?> target, Object targetObject, int modifiers)
            throws IllegalAccessException {
    }

    public static boolean verifyMemberAccess(Class<?> caller, Class<?> target, Object targetObject, int modifiers) {
        return false;
    }

    public static boolean isSubclassOf(Class<?> queryClass, Class<?> ofClass) {
        return false;
    }

    public static synchronized void registerFieldsToFilter(Class<?> containingClass, String... fieldNames) {
    }

    public static synchronized void registerMethodsToFilter(Class<?> containingClass, String... methodNames) {
    }

    /**
     * {@return the fields, unfiltered}
     *
     * <p>The loader's copy returned {@code null}, which NPEs at every call site. Nothing in this mod registers a
     * filter, so "no filtering" is the input array - the answer the JDK itself gives when no filter is registered.
     */
    public static Field[] filterFields(Class<?> containingClass, Field[] fields) {
        return fields;
    }

    /** {@return the methods, unfiltered} - see {@link #filterFields} for why this is not {@code null}. */
    public static Method[] filterMethods(Class<?> containingClass, Method[] methods) {
        return methods;
    }

    public static boolean isCallerSensitive(Method method) {
        return false;
    }

}
