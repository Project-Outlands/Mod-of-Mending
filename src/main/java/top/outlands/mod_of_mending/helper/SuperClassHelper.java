package top.outlands.mod_of_mending.helper;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The common superclass of two classes, answered the same whichever argument comes first.
 */
public final class SuperClassHelper {

    private static final String OBJECT = "java/lang/Object";

    /** Per class file, kept for the process: a jar cannot change while the game it belongs to is starting. */
    private static final Map<String, Optional<Info>> CACHE = new ConcurrentHashMap<>();

    private SuperClassHelper() {
    }

    /** What the walk needs from one class file. */
    private record Info(@Nullable String superName, boolean isInterface) {
    }

    /**
     * @param type1 a class in the runtime (deobfuscated) spelling, as ASM hands it over
     * @param type2 the other one
     * @return their nearest common superclass, {@code java/lang/Object} when there is nothing better to name
     */
    public static String getCommonSuperClass(String type1, String type2) {
        if (type1.equals(type2)) {
            return type1;
        }
        if (OBJECT.equals(type1) || OBJECT.equals(type2)) {
            return OBJECT;
        }
        final Optional<Info> info1 = info(type1);
        final Optional<Info> info2 = info(type2);
        if (info1.isEmpty() || info2.isEmpty()) {
            return OBJECT;
        }
        // Two interfaces, or a class and an interface: their only shared supertype this walk can name is Object,
        // which is also what ASM's own implementation answers.
        if (info1.get().isInterface() || info2.get().isInterface()) {
            return OBJECT;
        }
        final Set<String> ancestors = chain(type1);
        // The chains are ordered from the class upwards, so the first name they share is the nearest one.
        for (String candidate : chain(type2)) {
            if (ancestors.contains(candidate)) {
                return candidate;
            }
        }
        return OBJECT;
    }

    /** @return {@code type} itself, then every superclass up to and including {@code java/lang/Object} */
    private static Set<String> chain(String type) {
        final Set<String> chain = new LinkedHashSet<>();
        String current = type;
        while (current != null && !OBJECT.equals(current) && chain.add(current)) {
            current = info(current).map(Info::superName).orElse(null);
        }
        chain.add(OBJECT);
        return chain;
    }

    private static Optional<Info> info(String internalName) {
        return CACHE.computeIfAbsent(internalName, SuperClassHelper::read);
    }

    private static Optional<Info> read(String internalName) {
        try {
            final ClassReader reader = new ClassReader(classBytes(internalName));
            final String superName = reader.getSuperName();
            return Optional.of(new Info(
                    superName == null ? null : FMLDeobfuscatingRemapper.INSTANCE.map(superName),
                    (reader.getAccess() & Opcodes.ACC_INTERFACE) != 0));
        } catch (Throwable t) {
            return Optional.empty();
        }
    }

    /**
     * @return the bytes of the class file {@code internalName} is loaded from
     *
     * <p>The runtime name is the deobfuscated one while the entry on disk carries the obfuscated one, so the unmapped
     * spelling is tried first and the name itself second (mod jars are not obfuscated).
     */
    private static byte[] classBytes(String internalName) throws IOException {
        final String onDisk = FMLDeobfuscatingRemapper.INSTANCE.unmap(internalName);
        final byte[] bytes = readResource(onDisk + ".class");
        if (bytes != null) {
            return bytes;
        }
        final byte[] spelled = onDisk.equals(internalName) ? null : readResource(internalName + ".class");
        if (spelled != null) {
            return spelled;
        }
        throw new IOException("No class file for " + internalName);
    }

    @Nullable
    private static byte[] readResource(String resource) throws IOException {
        // parent first, so a JDK class answers from the platform and a mod class from the launch class loader
        final URL url = Launch.classLoader.getResource(resource);
        if (url == null) {
            return null;
        }
        try (InputStream in = url.openStream()) {
            return in.readAllBytes();
        }
    }
}
