package cc.ruok.hammer.engine;

public class EngineException extends Exception {

    private String msg;

    public EngineException(String msg) {
        this.msg = msg;
    }

    public EngineException(Exception e) {
        this.msg = e.getMessage();
    }

    @Override
    public String getMessage() {
        return msg;
    }
}
