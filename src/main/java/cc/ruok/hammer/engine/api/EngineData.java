package cc.ruok.hammer.engine.api;

import cc.ruok.hammer.engine.Engine;

import java.io.File;

public class EngineData extends EngineAPI {

    private byte[] bytes;

    public EngineData(byte[] bytes, Engine engine) {
        super(engine);
        this.bytes = bytes;
    }

    protected byte[] getBytes() {
        return bytes;
    }

    public void write(String path) throws EngineException {
        EngineFile file = new EngineFile(new File(engine.getWebSite().getPath() + "/" + path), engine);
        file.write(this);
    }

    @Override
    public String getVarName() {
        return null;
    }

    @Override
    public String toString() {
        return "Data(" + bytes.length + ")";
    }
}
