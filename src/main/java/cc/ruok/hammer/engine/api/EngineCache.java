package cc.ruok.hammer.engine.api;

import cc.ruok.hammer.engine.Engine;

public class EngineCache extends EngineAPI {

    public EngineCache(Engine engine) {
        super(engine);
    }

    public void put(String key, Object value) {
        engine.getWebSite().putCache(key, value);
    }

    public void put(String key, Object value, long time) {
        put(key, value);
    }

    public Object get(String key) {
        return engine.getWebSite().getCache(key);
    }

    public void remove(String key) {
        engine.getWebSite().removeCache(key);
    }

    @Override
    public String getVarName() {
        return "Cache";
    }
}
