package cc.ruok.hammer.plugin;

public abstract class HammerPlugin {

    protected PluginDescription description;

    public PluginDescription getDescription() {
        return description;
    }

    public void onLoad() {}

    public abstract void onDisable();

    public abstract void onEnable();

}
