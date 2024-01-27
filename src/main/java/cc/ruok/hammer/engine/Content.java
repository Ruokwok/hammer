package cc.ruok.hammer.engine;

public class Content {

    private String content;
    private boolean isScript;
    private int line;

    public Content(String content, boolean isScript) {
        this.content = content;
        this.isScript = isScript;
        if (isScript) this.content = content += " ";
        line = content.split("\n").length;
    }

    public boolean isScript() {
        return isScript;
    }

    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return content;
    }

}
