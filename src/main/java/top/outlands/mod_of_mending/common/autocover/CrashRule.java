package top.outlands.mod_of_mending.common.autocover;

import java.util.function.Predicate;

/**
 * One recognised crash shape: how to recognise it, and which universal patch (if any) can be taught about it.
 *
 * <p>{@code patch == null} means the shape is only worth explaining in the crash report - either it needs a JVM flag,
 * or the universal patch physically cannot help.
 */
public record CrashRule(String id, String description, Predicate<Diagnosis> matches, UniversalPatch patch,
        String hint) {

    public static CrashRule auto(String id, String description, Predicate<Diagnosis> matches, UniversalPatch patch) {
        return new CrashRule(id, description, matches, patch, null);
    }

    public static CrashRule hint(String id, String description, Predicate<Diagnosis> matches, String hint) {
        return new CrashRule(id, description, matches, null, hint);
    }

    public boolean autoAdds() {
        return this.patch != null;
    }
}
