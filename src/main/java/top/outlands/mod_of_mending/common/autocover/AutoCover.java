package top.outlands.mod_of_mending.common.autocover;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.fml.common.CrashReportHooks;
import net.minecraftforge.fml.common.ICrashReportContributor;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.outlands.mod_of_mending.Reference;
import top.outlands.mod_of_mending.config.ModOfMendingConfig;

import java.io.File;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns a crash that no universal patch covers into a config entry that covers it next launch.
 *
 * <p>The universal patches are semantics-free bytecode rewrites driven by class name lists, so "covering" a crash is
 * just "put this class in that list". Which list is decided by the shape of the crash, which class is decided by
 * running the patch' own transformer over the raw bytes of the frames in the stack: only a class whose bytes really
 * change is added, which is what keeps this from being guesswork.
 *
 * <p>Two entry points:
 * <ul>
 *   <li>{@link #installEarly()} - before {@code ConfigManager.register}, so a config written by the previous launch is
 *       already in effect; also replays reports of crashes that never reached a live throwable (launch, transformers)
 *       and registers {@link #describe(Throwable)} with the loader's crash report hooks.</li>
 *   <li>{@link #describe(Throwable)} - called by the loader while a crash report is written, for crashes that do
 *       produce one.</li>
 * </ul>
 */
public final class AutoCover {

    private static final Logger LOGGER = LogManager.getLogger(Reference.MOD_NAME);

    private static final String CONFIG_NAME = Reference.MOD_ID;
    private static final String STAMP_NAME = Reference.MOD_ID + ".autocover.stamp";
    private static final int MAX_ADDS_PER_LAUNCH = 2;
    private static final String AUTO_COVER_KEY = "Auto Cover Unpatched Crashes";
    private static final long REPORT_MAX_AGE = 7L * 24 * 60 * 60 * 1000;

    /** Packages whose classes are never the culprit of a mod-level crash. */
    private static final String[] FRAMEWORK_PREFIXES = {
            "java.", "javax.", "jdk.", "sun.", "com.sun.", "net.minecraft.", "net.minecraftforge.",
            "org.spongepowered.", "org.objectweb.asm.", "top.outlands.", "com.cleanroommc.", "zone.rong.",
            "org.apache.logging.log4j."
    };

    /**
     * Matches the three ways a throwable header can appear: bare (a crash report), {@code Caused by:} (a cause chain)
     * and {@code Exception in thread "main"} (a console log pasted as-is).
     */
    private static final Pattern REPORT_HEADER = Pattern.compile(
            "^\\s*(?:Caused by:\\s*)?(?:Exception in thread \"[^\"]*\"\\s*)?"
                    + "((?:[\\w$]+\\.)+[\\w$]+(?:Exception|Error|Throwable))(?:\\s*:\\s*(.*))?$");
    private static final Pattern ADD_OPENS =
            Pattern.compile("module (\\S+) does not \"?opens ([\\w.]+)\"?");

    private static final Map<Integer, String> HANDLED = new ConcurrentHashMap<>();

    private static final List<CrashRule> RULES = List.of(
            CrashRule.auto("cce_urlclassloader", "cast to java.net.URLClassLoader",
                    d -> d.is("ClassCastException") && d.says("cannot be cast to") && d.says("URLClassLoader"),
                    UniversalPatch.GET_URL),
            CrashRule.auto("cce_toarray", "Object[] cast to a typed array",
                    d -> d.is("ClassCastException") && d.says("[Ljava.lang.Object;"),
                    UniversalPatch.TO_ARRAY),
            CrashRule.auto("javax_ee_missing", "javax EE class no longer on the classpath",
                    d -> d.missingClassStartsWith("javax/xml/bind/") || d.missingClassStartsWith("javax/xml/ws/")
                            || d.missingClassStartsWith("javax/ws/") || d.missingClassStartsWith("javax/activation/")
                            || d.missingClassStartsWith("javax/soap/") || d.missingClassStartsWith("javax/jws/")
                            || d.missingClassStartsWith("javax/inject/"),
                    UniversalPatch.JAVAX),
            CrashRule.auto("oshi_moved", "oshi.SystemInfo moved to oshi.ffm",
                    d -> "oshi/SystemInfo".equals(d.missingClass()),
                    UniversalPatch.OSHI),
            CrashRule.auto("sun_reflection_gone", "sun.reflect.Reflection is gone",
                    d -> d.missingClassStartsWith("sun/reflect/Reflection")
                            || (d.is("NoSuchMethodError") && d.says("sun.reflect.Reflection.getCallerClass")),
                    UniversalPatch.SUN_REFLECTION),
            CrashRule.auto("launchwrapper_abi", "LaunchClassLoader ABI changed",
                    d -> (d.is("NoSuchMethodError") || d.is("NoSuchFieldError"))
                            && (d.says("LaunchClassLoader") || d.says("net.minecraft.launchwrapper.Launch.")),
                    UniversalPatch.LAUNCHWRAPPER),
            CrashRule.auto("guava_futures", "two-arg Futures.addCallback removed",
                    d -> d.is("NoSuchMethodError") && d.says("Futures.addCallback"),
                    UniversalPatch.FUTURES),
            CrashRule.auto("hashmap_computeifabsent", "recursive HashMap.computeIfAbsent",
                    d -> d.is("ConcurrentModificationException") && topFrameIs(d, "java.util.HashMap", "computeIfAbsent"),
                    UniversalPatch.COMPUTE_IF_ABSENT),
            CrashRule.auto("uuid_overflow", "UUID component overflows its bits",
                    d -> d.is("IllegalArgumentException") && d.says("UUID string too large"),
                    UniversalPatch.UUID),
            CrashRule.auto("uri_not_hierarchical", "URL.toURI is not hierarchical",
                    d -> d.is("IllegalArgumentException") && d.says("URI is not hierarchical"),
                    UniversalPatch.CODE_SOURCE),
            CrashRule.auto("final_field_write", "reflective write to a final field",
                    d -> d.is("IllegalAccessException") && d.says("Can not set") && d.says("final"),
                    UniversalPatch.REFLECTION),
            CrashRule.auto("scriptengine_null", "ScriptEngineManager returned no engine",
                    d -> d.is("NullPointerException"),
                    UniversalPatch.SCRIPT_ENGINE),
            CrashRule.hint("inaccessible_module", "module does not open the package",
                    d -> d.is("InaccessibleObjectException"), ""),
            CrashRule.hint("illegal_access_error", "JVM level access violation",
                    d -> d.is("IllegalAccessError"), "")
    );

    private AutoCover() {}

    /* ------------------------------------------------------------------ entry points */

    /**
     * Called from the loading plugin, before the config is registered: replays crash reports of the previous launch
     * and then marks this launch as seen, so that a report is handled exactly once.
     */
    public static void installEarly() {
        try {
            // the interface only has default methods, so it is not a functional interface; anonymous class it is
            CrashReportHooks.register(Reference.MOD_NAME, new ICrashReportContributor() {
                @Override
                public String getStackTraceReport(Throwable cause) {
                    return describe(cause);
                }
            });
        } catch (Throwable t) {
            LOGGER.warn("[AutoCover] could not register the crash report hook", t);
        }
        try {
            File stamp = new File(configDir(), STAMP_NAME);
            if (stamp.isFile()) {
                replayReports(stamp.lastModified());
            } else {
                LOGGER.debug("[AutoCover] first launch, existing crash reports are not replayed");
            }
            touch(stamp);
        } catch (Throwable t) {
            LOGGER.warn("[AutoCover] early install failed", t);
        }
    }

    /**
     * Registered as a {@link CrashReportHooks} contributor: never throws, and idempotent because the loader may ask
     * for the stack-trace text more than once per report.
     *
     * @return a block to append to the crash report, or an empty string
     */
    public static String describe(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        Integer key = System.identityHashCode(throwable);
        String cached = HANDLED.get(key);
        if (cached != null) {
            return cached;
        }
        String text;
        try {
            text = describeDiagnosis(Diagnosis.of(throwable));
        } catch (Throwable t) {
            LOGGER.warn("[AutoCover] could not classify the crash", t);
            text = "";
        }
        HANDLED.put(key, text);
        return text;
    }

    /* ------------------------------------------------------------------ core */

    /** Every rule that recognises this crash, in priority order. Used by the classifier below and by diagnostics. */
    static List<CrashRule> matchingRules(Diagnosis diagnosis) {
        List<CrashRule> matched = new ArrayList<>();
        for (CrashRule rule : RULES) {
            if (rule.matches().test(diagnosis)) {
                matched.add(rule);
            }
        }
        return matched;
    }

    static String describeDiagnosis(Diagnosis diagnosis) {
        CrashRule unverified = null;
        for (CrashRule rule : RULES) {
            if (!rule.matches().test(diagnosis)) {
                continue;
            }
            if (!rule.autoAdds()) {
                return report(rule, diagnosis, hintFor(rule, diagnosis), null, "hint");
            }
            if (!autoCoverEnabled()) {
                LOGGER.warn("[AutoCover] shape '{}' matched, but auto cover is disabled", rule.id());
                return report(rule, diagnosis,
                        "detected, but Auto Cover is disabled in the config", null, "disabled");
            }
            String culprit = findCulprit(diagnosis, rule.patch());
            if (culprit == null) {
                if (unverified == null) {
                    unverified = rule;
                }
                continue; // cannot prove this patch applies; give a later rule a chance
            }
            AddResult result = add(rule.patch(), culprit);
            LOGGER.warn("[AutoCover] shape '{}' -> class {}, patch '{}': {}", rule.id(), culprit, rule.patch().key,
                    result);
            return switch (result) {
                case ADDED -> report(rule, diagnosis,
                        "added to the universal patch list, takes effect on next launch", culprit, "added");
                case PRESENT -> report(rule, diagnosis,
                        "already covered by that list, so the patch itself did not apply - please report", culprit,
                        "ineffective");
                default -> report(rule, diagnosis,
                        "detected, but the config could not be written", culprit, "write-failed");
            };
        }

        if (unverified != null) {
            return report(unverified, diagnosis,
                    "this shape looks like '" + unverified.id() + "', but its transformer would not rewrite any class"
                            + " in the stack, so nothing was added", null, "unverified");
        }
        String candidate = candidate(diagnosis);
        return report(null, diagnosis, candidate.isEmpty() ? "" : "candidate: " + candidate, null, "unclassified");
    }

    private static String hintFor(CrashRule rule, Diagnosis diagnosis) {
        return switch (rule.id()) {
            case "inaccessible_module" -> {
                Matcher matcher = ADD_OPENS.matcher(diagnosis.describe());
                yield matcher.find()
                        ? "needs a JVM flag, no patch can help. Try: --add-opens " + matcher.group(1) + "/"
                                + matcher.group(2) + "=ALL-UNNAMED"
                        : "needs a JVM flag (--add-opens), no patch can help";
            }
            case "illegal_access_error" ->
                    "JVM level access violation; \"Final Fields Patching List\" may help, by hand";
            default -> "no automatic fix available";
        };
    }

    /**
     * For crashes no rule recognises: report which patches would rewrite a class in the stack, which is what a
     * maintainer needs to turn the report into a new rule.
     */
    private static String candidate(Diagnosis diagnosis) {
        Set<String> visited = new HashSet<>();
        for (StackTraceElement frame : diagnosis.frames()) {
            String className = frame.getClassName();
            if (className == null || !visited.add(className) || isFramework(className)) {
                continue;
            }
            byte[] raw = readRawClass(className);
            if (raw == null) {
                continue;
            }
            for (UniversalPatch patch : UniversalPatch.ALL) {
                // "any class that touches java.lang.reflect.Field" is far too weak to call a signal, and the last
                // two patches change behaviour rather than fixing a crash shape
                if (patch == UniversalPatch.REFLECTION || patch == UniversalPatch.CONNECTION
                        || patch == UniversalPatch.MOUSE_WHEEL) {
                    continue;
                }
                if (patch.wouldRewrite(raw)) {
                    return patch.key + " <- " + className;
                }
            }
        }
        return "";
    }

    private static String report(CrashRule rule, Diagnosis diagnosis, String action, String culprit, String kind) {
        StringBuilder out = new StringBuilder();
        out.append("\n\n-- ").append(Reference.MOD_NAME).append(" --\n");
        out.append("Detected: ").append(diagnosis.describe()).append('\n');
        if (rule != null) {
            out.append("Shape: ").append(rule.id()).append(" (").append(rule.description()).append(")\n");
        }
        if (culprit != null) {
            out.append("Class: ").append(culprit).append('\n');
            if (rule != null && rule.patch() != null) {
                out.append("Config: [").append(UniversalPatch.CATEGORY).append("] S:").append(rule.patch().key)
                        .append('\n');
            }
        }
        if (!action.isEmpty()) {
            out.append("Action: ").append(action).append('\n');
        }
        if (culprit != null && patchFile().isFile()) {
            out.append("Undo: remove that entry from ").append(patchFile().getPath()).append('\n');
        }
        String issue = issueUrl(rule, kind, culprit, diagnosis);
        if (!issue.isEmpty()) {
            out.append("Report: ").append(issue).append('\n');
        }
        return out.toString();
    }

    /* ------------------------------------------------------------------ culprit search */

    private static String findCulprit(Diagnosis diagnosis, UniversalPatch patch) {
        Set<String> seen = new HashSet<>();
        for (StackTraceElement frame : diagnosis.frames()) {
            String className = frame.getClassName();
            if (className == null || className.isEmpty() || !seen.add(className) || isFramework(className)) {
                continue;
            }
            byte[] raw = readRawClass(className);
            if (raw == null) {
                continue;
            }
            if (patch.wouldRewrite(raw)) {
                return className;
            }
        }
        return null;
    }

    private static boolean isFramework(String className) {
        for (String prefix : FRAMEWORK_PREFIXES) {
            if (className.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private static byte[] readRawClass(String className) {
        String resource = className.replace('.', '/') + ".class";
        try (InputStream in = Launch.classLoader.getResourceAsStream(resource)) {
            return in == null ? null : in.readAllBytes();
        } catch (Throwable t) {
            return null;
        }
    }

    private static boolean topFrameIs(Diagnosis diagnosis, String className, String methodPrefix) {
        StackTraceElement top = diagnosis.top();
        return top != null && className.equals(top.getClassName()) && top.getMethodName().startsWith(methodPrefix);
    }

    /* ------------------------------------------------------------------ config io */

    private enum AddResult {
        ADDED, PRESENT, FAILED
    }

    /**
     * Writes the class into the {@code custom_config} patch layer rather than into the generated config, so the entry
     * is reversible by deleting a line and survives a modpack update of {@code config/}.
     */
    private static synchronized AddResult add(UniversalPatch patch, String className) {
        try {
            Set<String> values = new LinkedHashSet<>();
            // The user's own file wins whenever it already has the key: a list that was emptied on purpose must stay
            // empty. Only an absent key falls back to the shipped defaults, otherwise writing the patch would drop
            // every default entry it did not know about.
            List<String> fromMain = readListOrNull(mainConfigFile(), patch.key);
            values.addAll(fromMain != null ? fromMain : Arrays.asList(patch.values()));
            values.addAll(readList(patchFile(), patch.key));
            if (values.contains(className)) {
                return AddResult.PRESENT;
            }
            values.add(className);
            return writeList(patch, values.toArray(new String[0])) ? AddResult.ADDED : AddResult.FAILED;
        } catch (Throwable t) {
            LOGGER.error("[AutoCover] could not add {} to {}", className, patch.key, t);
            return AddResult.FAILED;
        }
    }

    private static List<String> readList(File file, String key) {
        List<String> values = readListOrNull(file, key);
        return values == null ? Collections.emptyList() : values;
    }

    /** @return the list, or {@code null} when the file does not carry that key at all */
    private static List<String> readListOrNull(File file, String key) {
        if (file == null || !file.isFile()) {
            return null;
        }
        Configuration configuration = new Configuration(file);
        try {
            configuration.load();
            ConfigCategory category = configuration.getCategory(UniversalPatch.CATEGORY);
            Property property = category == null ? null : category.get(key);
            if (property == null || !property.isList()) {
                return null;
            }
            String[] values = property.getStringList();
            return values == null ? Collections.emptyList() : Arrays.asList(values);
        } catch (Throwable t) {
            LOGGER.warn("[AutoCover] could not read {} from {}", key, file, t);
            return null;
        }
    }

    private static boolean writeList(UniversalPatch patch, String[] values) {
        File file = patchFile();
        Configuration configuration = new Configuration(file);
        try {
            configuration.load();
            ConfigCategory category = configuration.getCategory(UniversalPatch.CATEGORY);
            Property property = category.get(patch.key);
            if (property == null || !property.isList()) {
                property = new Property(patch.key, values, Property.Type.STRING);
                category.put(patch.key, property);
            }
            property.setValues(values);
            property.setComment("Auto covered by " + Reference.MOD_NAME + " " + Reference.VERSION
                    + ": a crash report matched a known shape and this class was added automatically."
                    + " Delete an entry to undo it.");
            configuration.save();
            return true;
        } catch (Throwable t) {
            LOGGER.error("[AutoCover] could not write {}", file, t);
            return false;
        }
    }

    private static boolean autoCoverEnabled() {
        try {
            return ModOfMendingConfig.autoCover;
        } catch (Throwable t) {
            return true;
        }
    }

    /**
     * Same question, asked before {@code ConfigManager.register} has run: the field still holds the default, so the
     * value has to come from the file the user edited.
     */
    private static boolean autoCoverEnabledOnDisk() {
        File file = mainConfigFile();
        if (!file.isFile()) {
            return true;
        }
        Configuration configuration = new Configuration(file);
        try {
            configuration.load();
            ConfigCategory category = configuration.getCategory(UniversalPatch.CATEGORY);
            Property property = category == null ? null : category.get(AUTO_COVER_KEY);
            return property == null || property.getBoolean(true);
        } catch (Throwable t) {
            return true;
        }
    }

    /* ------------------------------------------------------------------ replay of old crash reports */

    private static void replayReports(long since) {
        File[] reports = crashReports();
        if (reports.length == 0) {
            return;
        }
        long oldest = System.currentTimeMillis() - REPORT_MAX_AGE;
        int added = 0;
        for (File report : reports) {
            if (added >= MAX_ADDS_PER_LAUNCH) {
                return;
            }
            if (report.lastModified() <= Math.max(since, oldest)) {
                continue;
            }
            try {
                if (replayOne(report)) {
                    added++;
                }
            } catch (Throwable t) {
                LOGGER.warn("[AutoCover] could not replay {}", report, t);
            }
        }
    }

    private static boolean replayOne(File file) throws Exception {
        if (!autoCoverEnabledOnDisk()) {
            return false;
        }
        String text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        Diagnosis diagnosis = parseReport(text);
        if (diagnosis == null) {
            return false;
        }
        LOGGER.info("[AutoCover] replaying {} from {}", diagnosis.describe(), file.getName());
        for (CrashRule rule : RULES) {
            if (!rule.autoAdds() || !rule.matches().test(diagnosis)) {
                continue;
            }
            String culprit = findCulprit(diagnosis, rule.patch());
            if (culprit == null) {
                continue;
            }
            AddResult result = add(rule.patch(), culprit);
            LOGGER.warn("[AutoCover] replay shape '{}' -> class {}, patch '{}': {}", rule.id(), culprit,
                    rule.patch().key, result);
            return result == AddResult.ADDED;
        }
        return false;
    }

    /**
     * Pulls the throwable chain and its frames out of a crash report. Stops at the first section after the stack
     * ({@code "-- Head --"} / {@code "A detailed walkthrough of the error"}), so that section stack traces do not
     * leak into the diagnosis.
     */
    static Diagnosis parseReport(String text) {
        List<String> types = new ArrayList<>();
        List<String> messages = new ArrayList<>();
        List<StackTraceElement> frames = new ArrayList<>();
        for (String raw : text.split("\r?\n")) {
            String line = raw.trim();
            if (line.startsWith("A detailed walkthrough of the error") || line.startsWith("-- ")) {
                break;
            }
            Matcher header = REPORT_HEADER.matcher(line);
            if (header.matches()) {
                types.add(header.group(1));
                messages.add(header.group(2) == null ? "" : header.group(2));
                continue;
            }
            if (line.startsWith("at ")) {
                StackTraceElement frame = parseFrame(line.substring(3));
                if (frame != null) {
                    frames.add(frame);
                }
            } else if (line.startsWith("... ") || line.startsWith("\t... ")) {
                // "... 3 more" - nothing to add
                continue;
            }
        }
        return types.isEmpty() ? null : Diagnosis.ofText(types, messages, frames);
    }

    private static StackTraceElement parseFrame(String text) {
        int open = text.indexOf('(');
        int close = text.lastIndexOf(')');
        if (open <= 0 || close < open) {
            return null;
        }
        String qualified = text.substring(0, open);
        String location = text.substring(open + 1, close);
        int module = qualified.indexOf('/'); // "java.base/java.util.HashMap.computeIfAbsent"
        if (module > 0) {
            qualified = qualified.substring(module + 1);
        }
        int dot = qualified.lastIndexOf('.');
        if (dot <= 0) {
            return null;
        }
        String className = qualified.substring(0, dot);
        String methodName = qualified.substring(dot + 1);
        String fileName = location.isEmpty() || location.startsWith("Unknown") || location.startsWith("Native")
                || location.startsWith("Hidden") ? null : location;
        int line = -1;
        if (fileName != null) {
            int colon = fileName.lastIndexOf(':');
            if (colon > 0) {
                try {
                    line = Integer.parseInt(fileName.substring(colon + 1));
                    fileName = fileName.substring(0, colon);
                } catch (NumberFormatException ignored) {
                    line = -1;
                }
            }
        }
        return new StackTraceElement(className, methodName, fileName, line);
    }

    private static File[] crashReports() {
        File dir = new File(instanceDir(), "crash-reports");
        File[] files = dir.listFiles((d, name) -> name.startsWith("crash-") && name.endsWith(".txt"));
        if (files == null || files.length == 0) {
            return new File[0];
        }
        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed()); // newest first
        return files;
    }

    /* ------------------------------------------------------------------ paths */

    private static File instanceDir() {
        File home = Launch.minecraftHome;
        return home == null ? new File(".") : home;
    }

    private static File configDir() {
        return new File(instanceDir(), "config");
    }

    private static File mainConfigFile() {
        return new File(configDir(), CONFIG_NAME + ".cfg");
    }

    private static File patchFile() {
        return new File(instanceDir(), "custom_config/" + CONFIG_NAME + ".cfg");
    }

    private static void touch(File file) {
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.isDirectory()) {
                parent.mkdirs();
            }
            if (!file.isFile() && !file.createNewFile()) {
                return;
            }
            file.setLastModified(System.currentTimeMillis());
        } catch (Throwable t) {
            LOGGER.debug("[AutoCover] could not touch {}", file, t);
        }
    }

    /* ------------------------------------------------------------------ issue link */

    private static String issueUrl(CrashRule rule, String kind, String culprit, Diagnosis diagnosis) {
        String base = Reference.ISSUES_URL;
        if (base == null || base.isBlank()) {
            return "";
        }
        String title = switch (kind) {
            case "ineffective" -> "[auto] patch did not apply: " + culprit + " (" + rule.patch().key + ")";
            case "unverified" -> "[auto] unverifiable shape: " + rule.id() + " @ " + diagnosis.describe();
            case "hint" -> "[auto] no automatic fix: " + rule.id() + " @ " + diagnosis.describe();
            case "write-failed" -> "[auto] could not write the config entry: " + rule.id();
            case "added" -> "[auto] newly covered: " + rule.id() + (culprit == null ? "" : " @ " + culprit);
            default -> rule != null ? "[auto] crash shape: " + rule.id() : "[auto] unclassified crash: "
                    + diagnosis.describe();
        };
        StringBuilder body = new StringBuilder();
        body.append(Reference.MOD_NAME).append(' ').append(Reference.VERSION).append('\n');
        body.append("Java: ").append(System.getProperty("java.version")).append('\n');
        if (rule != null) {
            body.append("Shape: ").append(rule.id()).append('\n');
        }
        if (culprit != null) {
            body.append("Class: ").append(culprit).append('\n');
            body.append("Config: [").append(UniversalPatch.CATEGORY).append("] S:").append(rule.patch().key)
                    .append('\n');
        }
        body.append("Crash: ").append(diagnosis.describe()).append('\n');
        body.append("\nPlease attach the full crash report.\n");
        return newIssueUrl(base) + "?title=" + encode(title) + "&body=" + encode(body.toString())
                + "&labels=auto-detected";
    }

    /** Accepts the tracker URL, the tracker's {@code /issues} URL or a ready {@code /issues/new} one. */
    private static String newIssueUrl(String base) {
        String trimmed = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        if (trimmed.endsWith("/new")) {
            return trimmed;
        }
        return trimmed.endsWith("/issues") ? trimmed + "/new" : trimmed + "/issues/new";
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
