package cc.ruok.hammer.engine;

import cc.ruok.hammer.Logger;

public class EngineSystem {

    private final Engine engine;

    public EngineSystem(Engine engine) {
        this.engine = engine;
    }

    public long getTime() {
        return System.currentTimeMillis();
    }

    public void info(String str) {
        Logger.info(str);
    }

    public void warning(String str) {
        Logger.warning(str);
    }

    public void output() {
        engine.output();
    }

    public void output(Object obj) {
        engine.output(obj.toString());
    }

}
