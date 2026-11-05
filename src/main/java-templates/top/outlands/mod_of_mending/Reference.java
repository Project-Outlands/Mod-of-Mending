package {{ package }};

/**
 * Tags storage class, you can change at will
 */
public class Reference {
    private Reference() {}

    public static final String MOD_ID = "{{ mod_id }}";
    public static final String MOD_NAME = "{{ mod_name }}";
    public static final String VERSION = "{{ mod_version }}";

    /** Where AutoCover points a player at when it sees a crash it cannot classify. Empty in a fork or a private build. */
    public static final String ISSUES_URL = "{{ mod_issue_tracker }}";

    /**
     * Read by {@code @Mod(dependencies = ...)}.
     *
     * <p>The loader ships as the {@code outlands} mod container, so that is the id a coremod written against this
     * toolchain has to require; the Forge floor stays because the patched Minecraft tree is still Forge's.
     */
    public static final String MOD_DEPENDENCIES = "required-after:forge@[14.23.5.2864,);required-after:outlands;";
}
