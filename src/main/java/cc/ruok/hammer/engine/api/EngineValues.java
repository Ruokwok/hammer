package cc.ruok.hammer.engine.api;

import cc.ruok.hammer.engine.Engine;

import java.util.HashMap;

public class EngineValues extends EngineAPI {

    private HashMap<String, Object> values;

    public EngineValues(Engine engine) {
        super(null);
        this.values = engine.getWebSite().getValues();
    }

    public Object get(String key) {
        return values.get(key);
    }

    public String getString(String key) {
        return (String) values.get(key);
    }

    public int getInt(String key) {
        return (int) values.get(key);
    }

    public long getLong(String key) {
        return (long) values.get(key);
    }

    public double getDouble(String key) {
        return (double) values.get(key);
    }

    public boolean getBoolean(String key) {
        return (boolean) values.get(key);
    }

    @Override
    public String getVarName() {
        return "Values";
    }
}
