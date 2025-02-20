package cc.ruok.hammer.engine.api;

import cc.ruok.hammer.engine.Engine;

public abstract class EngineAPI {

    Engine engine;

    public EngineAPI(Engine engine) {
        this.engine = engine;
    }

    public abstract String getVarName();

    @Override
    public String toString() {
        return "Object(" + this.getClass().getName() + ")";
    }

    public static void registerDefault() {
        Engine.registerAPI("System", EngineSystem.class);
        Engine.registerAPI("Codec", EngineCodec.class);
        Engine.registerAPI("Date", EngineDate.class);
        Engine.registerAPI("Http", EngineHttp.class);
        Engine.registerAPI("Digest", EngineDigest.class);
        Engine.registerAPI("Database", EngineDatabase.class);
        Engine.registerAPI("Log", EngineLog.class);
    }
}
