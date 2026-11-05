package top.outlands.mod_of_mending.helper;

import net.minecraftforge.fml.common.FMLLog;

import java.util.UUID;

/**
 * {@link UUID#fromString(String)} that still accepts what Java 8 accepted.
 *
 * <p>Java 8's parser let a component of a UUID run past its 16/48 allotted bits and simply dropped the overflow, so
 * strings produced by (or hand-edited for) old mods parse there and throw {@code UUID string too large} on every
 * later JDK. Mods that read such an id out of their config or their saved data fail to start.
 *
 * <p>The loader carried this as {@code net.minecraftforge.fml.javafix.UUIDFix}; Outlands 2026.11.05 deleted it (it
 * was kept only as a remap target) and there is no upstream library equivalent, so it lives here. The behaviour is the
 * original loaders', kept byte for byte: try the strict parser first, and only fall back when the failure is the
 * overflow one.
 */
public final class UUIDFix {

    private UUIDFix() { }

    public static UUID fromString(String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            if ("UUID string too large".equals(e.getMessage())) {
                return oldFromString(uuid);
            }
            throw e;
        }
    }

    private static UUID oldFromString(String uuid) {
        String[] components = uuid.split("-");
        if (components.length != 5) {
            throw new IllegalArgumentException("Invalid UUID string: " + uuid);
        }
        FMLLog.log.error("UUID [{}] is being processed with the approach from Java 8 for compatibility's sake. This UUID is malformed!", uuid);
        for (int i = 0; i < 5; i++) {
            components[i] = "0x" + components[i];
        }
        long mostSigBits = Long.decode(components[0]);
        mostSigBits <<= 16;
        mostSigBits |= Long.decode(components[1]);
        mostSigBits <<= 16;
        mostSigBits |= Long.decode(components[2]);

        long leastSigBits = Long.decode(components[3]);
        leastSigBits <<= 48;
        leastSigBits |= Long.decode(components[4]);

        return new UUID(mostSigBits, leastSigBits);
    }

}
