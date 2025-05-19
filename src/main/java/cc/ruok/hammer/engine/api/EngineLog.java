package cc.ruok.hammer.engine.api;

import cc.ruok.hammer.Logger;
import cc.ruok.hammer.engine.Engine;

public class EngineLog extends EngineAPI {

    public EngineLog(Engine engine) {
        super(engine);
    }

    public void info(Object object) {
        Logger.info("[" + engine.getWebSite().getName() + "]" + object.toString());
    }

    public void warning(Object object) {
        Logger.warning("[" + engine.getWebSite().getName() + "]" + object.toString());
    }

    public void debug(Object object) {
        Logger.debug("[" + engine.getWebSite().getName() + "]" + object.toString());
    }

    public void error(Object object) {
        Logger.error("[" + engine.getWebSite().getName() + "]" + object.toString());
    }

    @Override
    public String getVarName() {
        return "Log";
    }
}
