package cc.ruok.hammer.engine;

import java.util.LinkedList;

public class Script {

    private LinkedList<Content> list = new LinkedList<>();
    private String compile;
    private Engine engine;

    public Script(String str, Engine engine) {
        this.engine = engine;
        analysis(str);
        this.compile = compile();
    }

    public void analysis(String script) {
        if (!script.contains("<?hsp")) {
            list.add(new Content(script, false));
            return;
        }
        char[] chars = script.toCharArray();
        boolean inScript = false;
        boolean inString = false;
//        boolean qm = true;
        byte tag = 0;
        StringBuilder buffer = new StringBuilder();
        StringBuilder cache = new StringBuilder();
        for (char c : chars) {
            if (!inScript) {
                if (c == '<' && tag == 0) {
                    tag++;
                    cache.append(c);
                } else if (c == '?' && tag == 1) {
                    tag++;
                    cache.append(c);
                } else if (c == 'h' && tag == 2) {
                    tag++;
                    cache.append(c);
                } else if (c == 's' && tag == 3) {
                    tag++;
                    cache.append(c);
                } else if (c == 'p' && tag == 4) {
                    tag++;
                    cache.append(c);
                } else if ((c == ' ' || c == '\n' || c == '\r') && tag == 5) {
                    tag = 0;
                    inScript = true;
                    buffer.append(c);
                    list.add(new Content(buffer.toString(), false));
                    buffer = new StringBuilder();
                    cache = new StringBuilder();
                } else {
                    if (cache.length() > 0) {
                        buffer.append(cache);
                        cache = new StringBuilder();
                    }
                    tag = 0;
                    buffer.append(c);
                }
            } else {
                if (!inString && c == '"') {
                    inString = true;
//                    qm = true;
                    buffer.append(c);
                } else if (!inString && c == '\'') {
                    inString = true;
//                    qm = false;
                    buffer.append(c);
                } else if ((inString) && c == '"') {
                    inString = false;
                    buffer.append(c);
                } else if ((inString) && c == '\'') {
                    inString = false;
                    buffer.append(c);
                } else {
//                    if (inString && c == '\\') {
//
//                    }
                    if (!inString && c == '?' && tag == 0) {
                        tag++;
                        cache.append(c);
                    } else if (!inString && c == '>' && tag == 1) {
                        tag = 0;
                        inScript = false;
                        buffer.append(';');
                        list.add(new Content(buffer.toString(), true));
                        buffer = new StringBuilder();
                        cache = new StringBuilder();
                    } else {
                        if (cache.length() > 0) {
                            buffer.append(cache);
                            cache = new StringBuilder();
                        }
                        tag = 0;
                        buffer.append(c);
                    }
                }
            }
        }
        list.add(new Content(buffer.toString(), inScript));
    }

    public String compile() {
        StringBuilder sb = new StringBuilder();
        for (Content content : list) {
            if (content.isScript()) {
                sb.append(content);
            } else {
                int line = content.getLine();
                String key = Integer.toHexString(content.hashCode());
                engine.putOutput(key, content.toString());
                sb.append("System.output('").append(key).append("');");
                sb.append("\n".repeat(Math.max(0, line - 1)));
            }
        }
        return sb.toString();
    }

    public String getCompile() {
        return compile;
    }

}
