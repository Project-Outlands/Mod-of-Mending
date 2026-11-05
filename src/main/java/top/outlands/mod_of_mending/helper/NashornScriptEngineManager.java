package top.outlands.mod_of_mending.helper;

import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptEngineManager;

/**
 * A {@link ScriptEngineManager} that always knows about Nashorn.
 *
 * <p>{@code ScriptEngineManager} discovers engines through the thread context class loader. Mods that build one
 * inside a launch class loader therefore see nothing, and every {@code getEngineByName("nashorn")} answers
 * {@code null}. This subclass registers the factory it already has instead of looking for one.
 *
 * <p>This used to live in the loader ({@code com.cleanroommc.loader.scripting.CleanroomScriptEngineManager}) and was
 * dropped there because nothing referenced it; the transformers below are the reference, so it lives here now.
 */
public class NashornScriptEngineManager extends ScriptEngineManager {

    public NashornScriptEngineManager() {
        super(Thread.currentThread().getContextClassLoader());
    }

    public NashornScriptEngineManager(ClassLoader loader) {
        initialize();
    }

    private void initialize() {
        NashornScriptEngineFactory js = new NashornScriptEngineFactory();
        for (String name : js.getNames()) {
            this.registerEngineName(name, js);
        }
        for (String mime : js.getMimeTypes()) {
            this.registerEngineMimeType(mime, js);
        }
        for (String ext : js.getExtensions()) {
            this.registerEngineExtension(ext, js);
        }
    }

}
