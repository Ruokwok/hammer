package cc.ruok.hammer.engine.api;

import cc.ruok.hammer.Hammer;
import cc.ruok.hammer.Logger;
import cc.ruok.hammer.engine.Engine;
import cc.ruok.hammer.engine.HttpEngine;
import cc.ruok.hammer.engine.Script;
import cn.hutool.json.JSONUtil;
import org.graalvm.polyglot.Context;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.*;

public class EngineSystem extends EngineAPI{
    private final List<String> includeList = new ArrayList<>();

    public EngineSystem(Engine engine) {
        super(engine);
    }

    @Override
    public String getVarName() {
        return "System";
    }

    public long getTime() {
        return System.currentTimeMillis();
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
            if (obj instanceof String[] && ((String[]) obj).length == 1) {
                engine.outputScript(((String[]) obj)[0]);
            } else if (obj instanceof Collection<?> || obj instanceof Map<?,?> || obj instanceof Object[]) {
                engine.outputScript(JSONUtil.toJsonStr(obj));
            } else {
                engine.outputScript(obj.toString());
            }
        }
    }

    public void include(String str, String file) throws EngineException {
        if (includeList.contains(file)) {
            throw new EngineException("prohibit circular include.");
        } else {
            includeList.add(file);
            if (file.endsWith(".js")) engine.getContext().eval("js", str);
            Script script = new Script(str, engine);
            String compile = script.getCompile();
            engine.getContext().eval("js", compile);
        }
    }

    public void setStatus(int code) throws EngineException {
        engine.setStatus(code);
    }

    public EngineFile getFile(String filename) {
        return new EngineFile(new File(engine.getWebSite().getPath() + "/" + filename), engine);
    }

    public void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            Logger.logException(e);
        }
    }


    public Object module(String name) throws EngineException {
        Object object = HttpEngine.getModule(name);
        if (object == null) throw new EngineException("Unknown module: " + name);
        if (object instanceof Class<?> apiClass) {
            if (EngineAPI.class.isAssignableFrom(apiClass)) {
                try {
                    Constructor<? extends EngineAPI> constructor = (Constructor<? extends EngineAPI>) apiClass.getDeclaredConstructor(Engine.class);
                    EngineAPI api = constructor.newInstance(this.engine);
                    return api;
                } catch (Exception e) {
                    Logger.logException(e);
                }
            } else {
                try {
                    Constructor<?> constructor = apiClass.getConstructor();
                    return constructor.newInstance();
                } catch (Exception e) {
                    Logger.logException(e);
                }
            }
        } else {
            return object;
        }
        return null;
    }

    public void stop(int code) throws EngineException {
        engine.finish();
        if (code == -1) {
            engine.close();
        } else {
            engine.close(code);
        }
    }

    public String getVersion() {
        return Hammer.getVersion();
    }

    public void task(String url) throws EngineException {
        String path = null;
        if (url.contains("?")) {
            path = url.substring(0, url.indexOf("?"));
        } else {
            path = url;
        }
        EngineFile file = getFile(path);
        if (file.exists() && file.isFile()) {
            try {
                Engine task = new Engine(file.readString(), url, new Engine.NullWriter(), engine.getWebSite());
                task.setRT(new EngineSystem(task));
                long start = System.currentTimeMillis();
                String finalPath = path;
                new Thread(() -> {
                    task.execute();
                    Logger.info("[" + engine.getWebSite().getName() + "][TASK][" + task.getStatus() + "] - " + finalPath + "(" + (System.currentTimeMillis() - start) + "ms)");
                }).start();
            } catch (Exception e) {
                throw new EngineException(e.getMessage());
            }
        } else {
            throw new EngineException("The file does not exist:" + path);
        }
    }

    public void finish() {

    }
}
