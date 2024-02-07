package cc.ruok.hammer.engine;

import cc.ruok.hammer.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EngineSystem {

    private final Engine engine;
    private final List<String> includeList = new ArrayList<>();

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

    public void output(Object obj) {
        if (obj == null) {
            engine.outputStatic(null);
        } else {
            engine.outputStatic(obj.toString());
        }
    }

    public void outputScript(Object obj) {
        if (obj == null) {
            engine.outputScript(null);
        } else {
            engine.outputScript(obj.toString());
        }
    }

    public String include(String str, String file) throws EngineException {
        Script script = new Script(str, engine);
        if (includeList.contains(file)) {
            throw new EngineException("prohibit circular include.");
        } else {
            includeList.add(file);
            return script.getCompile();
        }
    }

    public Map<String, Object> getSession(int time) {
        engine.getSession().setMaxInactiveInterval(time);
        return engine.getSessionData();
    }

    public void saveSession(Object obj) {
        if (obj instanceof Map<?,?>) {
            engine.saveSessionData((Map<String, Object>) obj);
        }
    }

    public void sessionClose() {
        engine.destroySession();
    }

}
