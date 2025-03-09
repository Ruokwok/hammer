package cc.ruok.hammer.engine.api;

public interface Closeable {

    void close() throws EngineException;

    void keep();

    boolean isKeep();

}
