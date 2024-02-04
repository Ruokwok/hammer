package cc.ruok.hammer.engine;

import java.io.File;

public class EngineFiles {

    private final Engine engine;

    public EngineFiles(Engine engine) {
        this.engine = engine;
    }

    public EngineFile getFile(String filename) {
        return new EngineFile(new File(engine.getWebSite().getPath() + "/" + filename));
    }

}
