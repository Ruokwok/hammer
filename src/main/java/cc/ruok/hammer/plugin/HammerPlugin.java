package cc.ruok.hammer.plugin;

public abstract class HammerPlugin {

    protected PluginDescription description;

    public HammerPlugin(PluginDescription description) {
        this.description = description;
    }

    public PluginDescription getDescription() {
        return description;
    }

    public abstract void onEnable();

}
