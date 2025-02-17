package cc.ruok.hammer.plugin;

public abstract class HammerPlugin {

    protxected PluginDescription description;

    public HammerPlugin(PluginDescription description) {
        this.description = description;
    }

    public PluginDescription getDescription() {
        return description;
    }

    public void onLoad() {}

    public abstract void onDisable();

    public abstract void onEnable();

}
