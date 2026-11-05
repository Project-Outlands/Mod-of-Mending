package top.outlands.mod_of_mending.common.autocover;

import top.outlands.foundation.IExplicitTransformer;
import top.outlands.mod_of_mending.config.ModOfMendingConfig;
import top.outlands.mod_of_mending.transformer.universal.AddFutureCallbackTransformer;
import top.outlands.mod_of_mending.transformer.universal.ComputeIfAbsentTransformer;
import top.outlands.mod_of_mending.transformer.universal.ITweakerTransformer;
import top.outlands.mod_of_mending.transformer.universal.MalformedUUIDTransformer;
import top.outlands.mod_of_mending.transformer.universal.ReflectFieldTransformer;
import top.outlands.mod_of_mending.transformer.universal.RemapLegacyLWTransformer;
import top.outlands.mod_of_mending.transformer.universal.RemapSunReflectionTransformer;
import top.outlands.mod_of_mending.transformer.universal.RemapTransformer;
import top.outlands.mod_of_mending.transformer.universal.ScriptEngineTransformer;
import top.outlands.mod_of_mending.transformer.universal.ToArrayTransformer;
import top.outlands.mod_of_mending.transformer.universal.URLClassLoaderTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.function.Supplier;

/**
 * One list-shaped universal patch: the config entry that lists its targets, plus a way to build the transformer it
 * drives.
 *
 * <p>The transformer factory exists so that {@link AutoCover} can run the very same rewrite over a class' raw bytes
 * before deciding that the class belongs in that list. Keeping the list and the transformer in one place is what
 * makes the guess verifiable rather than heuristic.
 */
public final class UniversalPatch {

    /** The config category all of these lists live in ({@code @Config.category()} defaults to "general"). */
    public static final String CATEGORY = "general";

    /** {@code javax.*} packages that no longer exist on modern JDKs, mirrored into {@code jakarta.*}. */
    public static final String[] JAVAX_FROM = {
            "javax/xml/bind/", "javax/xml/ws/", "javax/ws/", "javax/activation/", "javax/soap/", "javax/jws/",
            "javax/inject/"
    };
    public static final String[] JAVAX_TO = {
            "jakarta/xml/bind/", "jakarta/xml/ws/", "jakarta/ws/", "jakarta/activation/", "jakarta/soap/",
            "jakarta/jws/", "jakarta/inject/"
    };
    public static final String[] OSHI_FROM = {"oshi/SystemInfo"};
    public static final String[] OSHI_TO = {"oshi/ffm/SystemInfo"};

    public final String key;
    private final Supplier<String[]> values;
    private final Supplier<IExplicitTransformer> transformer;

    private UniversalPatch(String key, Supplier<String[]> values, Supplier<IExplicitTransformer> transformer) {
        this.key = key;
        this.values = values;
        this.transformer = transformer;
    }

    /** The list as the game currently has it (main config merged with the patch layer, once registered). */
    public String[] values() {
        try {
            String[] list = this.values.get();
            return list == null ? new String[0] : list;
        } catch (Throwable t) {
            return new String[0];
        }
    }

    /**
     * @param raw untransformed class bytes
     * @return whether the transformer of this patch would actually change that class
     */
    public boolean wouldRewrite(byte[] raw) {
        try {
            IExplicitTransformer instance = this.transformer.get();
            byte[] rewritten = instance.transform(raw.clone());
            if (rewritten == null) {
                return false;
            }
            if (Arrays.equals(raw, rewritten)) {
                return false;
            }
            // Almost every transformer rebuilds the class through ClassWriter, which re-lays the constant pool even
            // when it changed nothing, so byte equality proves nothing. Compare what the class *is* instead: a
            // no-op round trip dumps identically, a real rewrite does not.
            return !dump(raw).equals(dump(rewritten));
        } catch (Throwable t) {
            return false; // cannot prove it would help, so do not claim it
        }
    }

    /** Text form of the class (members and instructions, not the constant pool layout). */
    private static String dump(byte[] bytes) {
        StringWriter writer = new StringWriter();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(new TraceClassVisitor(new PrintWriter(writer)), 0);
        return writer.toString();
    }

    public static final UniversalPatch REFLECTION = new UniversalPatch(
            "Reflection Patch Target List", () -> ModOfMendingConfig.reflectionPatchTargets, ReflectFieldTransformer::new);
    public static final UniversalPatch GET_URL = new UniversalPatch(
            "Get URL Patch Target List", () -> ModOfMendingConfig.getURLPatchTargets, URLClassLoaderTransformer::new);
    public static final UniversalPatch SCRIPT_ENGINE = new UniversalPatch(
            "New Script Engine Patch Target List", () -> ModOfMendingConfig.scriptEngineTargets,
            ScriptEngineTransformer::new);
    public static final UniversalPatch UUID = new UniversalPatch(
            "UUID Patch Target List", () -> ModOfMendingConfig.UUIDTargets, MalformedUUIDTransformer::new);
    public static final UniversalPatch JAVAX = new UniversalPatch(
            "Javax Patch Target List", () -> ModOfMendingConfig.remapTargets,
            () -> new RemapTransformer(JAVAX_FROM, JAVAX_TO));
    public static final UniversalPatch LAUNCHWRAPPER = new UniversalPatch(
            "Launch Wrapper API Change Patching List", () -> ModOfMendingConfig.remapLWTargets,
            RemapLegacyLWTransformer::new);
    public static final UniversalPatch OSHI = new UniversalPatch(
            "OSHI Remap List", () -> ModOfMendingConfig.remapOshiTargets,
            () -> new RemapTransformer(OSHI_FROM, OSHI_TO));
    public static final UniversalPatch SUN_REFLECTION = new UniversalPatch(
            "sun.misc.Reflection Patching List", () -> ModOfMendingConfig.remapReflectionTargets,
            RemapSunReflectionTransformer::new);
    public static final UniversalPatch CODE_SOURCE = new UniversalPatch(
            "getCodeSource() Patching List", () -> ModOfMendingConfig.getCodeSourcePatchTargets,
            ITweakerTransformer::new);
    public static final UniversalPatch COMPUTE_IF_ABSENT = new UniversalPatch(
            "computeIfAbsent Patching List", () -> ModOfMendingConfig.computeIfAbsentTargets,
            ComputeIfAbsentTransformer::new);
    public static final UniversalPatch FUTURES = new UniversalPatch(
            "Futures.addCallback Patching List", () -> ModOfMendingConfig.addFutureCallbackTargets,
            AddFutureCallbackTransformer::new);
    public static final UniversalPatch TO_ARRAY = new UniversalPatch(
            "List.toArray() Patching List", () -> ModOfMendingConfig.toArrayPatchTargets, ToArrayTransformer::new);
    public static final UniversalPatch MOUSE_WHEEL = new UniversalPatch(
            "Mouse.getEventDWheel() Patching List", () -> ModOfMendingConfig.mouseWheelPatchTargets,
            () -> new top.outlands.mod_of_mending.transformer.universal.DWheelTransformer());
    public static final UniversalPatch CONNECTION = new UniversalPatch(
            "Connection Blocking List", () -> ModOfMendingConfig.nonUpdateTargets,
            () -> new top.outlands.mod_of_mending.transformer.universal.ConnectionBlockingTransformer());

    /** Every list-shaped patch, mostly for diagnostics. */
    public static final UniversalPatch[] ALL = {
            REFLECTION, GET_URL, SCRIPT_ENGINE, UUID, JAVAX, LAUNCHWRAPPER, OSHI, SUN_REFLECTION, CODE_SOURCE,
            COMPUTE_IF_ABSENT, FUTURES, TO_ARRAY, MOUSE_WHEEL, CONNECTION
    };
}
