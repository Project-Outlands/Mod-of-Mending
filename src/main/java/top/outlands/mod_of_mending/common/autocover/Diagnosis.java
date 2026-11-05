package top.outlands.mod_of_mending.common.autocover;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A normalised view over a throwable, or over a throwable parsed back out of a crash report.
 *
 * <p>Everything the rules need to look at is flattened here so that a live crash and a report read on the next
 * launch go through exactly the same matching code: simple type names and messages of the whole cause chain, the
 * stack frames of every link of that chain, and the class named by a {@code NoClassDefFoundError}.
 */
public final class Diagnosis {

    /** Simple names of the throwable and its causes, outermost first. */
    private final List<String> types;
    /** Messages of the throwable and its causes, outermost first, never {@code null}. */
    private final List<String> messages;
    /** Stack frames of every link of the chain, innermost cause first (the real crash site is usually in there). */
    private final List<StackTraceElement> frames;
    /** The class named by a {@code NoClassDefFoundError}/{@code ClassNotFoundException}, or {@code null}. */
    private final String missingClass;

    private Diagnosis(List<String> types, List<String> messages, List<StackTraceElement> frames, String missingClass) {
        this.types = types;
        this.messages = messages;
        this.frames = frames;
        this.missingClass = missingClass;
    }

    public static Diagnosis of(Throwable throwable) {
        List<String> types = new ArrayList<>();
        List<String> messages = new ArrayList<>();
        List<List<StackTraceElement>> stacks = new ArrayList<>();
        String missingClass = null;

        for (Throwable t = throwable; t != null; t = t.getCause() == t ? null : t.getCause()) {
            types.add(t.getClass().getSimpleName());
            String message = t.getMessage();
            messages.add(message == null ? "" : message);
            stacks.add(List.of(t.getStackTrace()));
            if (missingClass == null && (t instanceof NoClassDefFoundError || t instanceof ClassNotFoundException)) {
                missingClass = normaliseMissingClass(message);
            }
            if (stacks.size() > 16) {
                break; // a cause loop, or something pathological
            }
        }

        List<StackTraceElement> frames = new ArrayList<>();
        for (int i = stacks.size() - 1; i >= 0; i--) { // innermost cause first
            frames.addAll(stacks.get(i));
        }
        return new Diagnosis(types, messages, frames, missingClass);
    }

    /**
     * Rebuild a diagnosis from a crash report: every throwable header of the report (the primary one plus its
     * {@code Caused by:} chain) and the frames collected from all of them.
     */
    public static Diagnosis ofText(List<String> qualifiedTypes, List<String> messages, List<StackTraceElement> frames) {
        List<String> types = new ArrayList<>();
        String missingClass = null;
        for (int i = 0; i < qualifiedTypes.size(); i++) {
            String qualified = qualifiedTypes.get(i);
            int lastDot = qualified.lastIndexOf('.');
            String simpleName = lastDot < 0 ? qualified : qualified.substring(lastDot + 1);
            types.add(simpleName);
            String message = i < messages.size() ? messages.get(i) : "";
            if (missingClass == null
                    && (simpleName.endsWith("NoClassDefFoundError") || simpleName.endsWith("ClassNotFoundException"))) {
                missingClass = normaliseMissingClass(message);
            }
        }
        return new Diagnosis(types, messages, frames, missingClass);
    }

    private static String normaliseMissingClass(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        String value = message.trim();
        if (value.startsWith("Could not initialize class")) {
            return null; // failed static init, not a missing class
        }
        int space = value.indexOf(' ');
        if (space > 0) {
            value = value.substring(0, space); // strip a trailing description
        }
        if (value.indexOf('/') < 0 && value.indexOf('.') > 0) {
            value = value.replace('.', '/');
        }
        return value;
    }

    /* ------------------------------------------------------------------ queries */

    public boolean is(String simpleTypeName) {
        return types.contains(simpleTypeName);
    }

    /** True when any link of the chain has a message containing the given text. */
    public boolean says(String needle) {
        for (String message : messages) {
            if (message.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    public String missingClass() {
        return missingClass;
    }

    public boolean missingClassStartsWith(String prefix) {
        return missingClass != null && missingClass.startsWith(prefix);
    }

    public List<StackTraceElement> frames() {
        return Collections.unmodifiableList(frames);
    }

    /** First frame, used by the shapes whose crash site is the top of the stack. */
    public StackTraceElement top() {
        return frames.isEmpty() ? null : frames.get(0);
    }

    public String describe() {
        return types.isEmpty() ? "?" : types.get(0) + (messages.get(0).isEmpty() ? "" : ": " + messages.get(0));
    }

    @Override
    public String toString() {
        return describe();
    }
}
