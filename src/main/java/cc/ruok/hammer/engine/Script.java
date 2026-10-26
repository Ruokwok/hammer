package cc.ruok.hammer.engine;

import java.util.LinkedList;

public class Script {

    private LinkedList<Content> list = new LinkedList<>();
    private String compile;
    private Engine engine;

    public Script(String str, Engine engine) {
        this(str, engine, false);
    }

    public Script(String str, Engine engine, boolean fullScript) {
        this.engine = engine;
        analysis(str, fullScript);
        this.compile = compile();
    }

    public static boolean isFullScript(String filename) {
        return filename != null && filename.toLowerCase().endsWith(".hspx");
    }

    public void analysis(String script) {
        analysis(script, false);
    }

    public void analysis(String script, boolean fullScript) {
        if (fullScript) {
            list.add(new Content(script, true));
            return;
        }
        if (!script.contains("<?hsp")) {
            list.add(new Content(script, false));
            return;
        }
        String open = "<?hsp";
        char[] s = script.toCharArray();
        int n = s.length;
        StringBuilder buffer = new StringBuilder();
        StringBuilder code = new StringBuilder();
        boolean inScript = false;
        int mode = 0;
        char quote = 0;
        boolean escaped = false;
        int i = 0;
        while (i < n) {
            if (!inScript) {
                if (s[i] == '<' && i + open.length() <= n && script.startsWith(open, i)) {
                    int j = i + open.length();
                    if (j < n && Character.isWhitespace(s[j])) {
                        inScript = true;
                        buffer.append(s[j]);
                        list.add(new Content(buffer.toString(), false));
                        buffer = new StringBuilder();
                        i = j + 1;
                        continue;
                    }
                }
                buffer.append(s[i]);
                i++;
            } else {
                char c = s[i];
                if (mode == 4) {
                    if (c == '\n' || c == '\r') mode = 0;
                    code.append(c);
                    i++;
                    continue;
                }
                if (mode == 3) {
                    if (c == '*' && i + 1 < n && s[i + 1] == '/') {
                        code.append("*/");
                        i += 2;
                        mode = 0;
                    } else {
                        code.append(c);
                        i++;
                    }
                    continue;
                }
                if (mode != 0) {
                    if (escaped) {
                        escaped = false;
                    } else if (c == '\\') {
                        escaped = true;
                    } else if (c == quote) {
                        mode = 0;
                    }
                    code.append(c);
                    i++;
                    continue;
                }
                if (c == '"' || c == '\'' || c == '`') {
                    mode = 1;
                    quote = c;
                    escaped = false;
                    code.append(c);
                    i++;
                    continue;
                }
                if (c == '/' && i + 1 < n && (s[i + 1] == '/' || s[i + 1] == '*')) {
                    mode = s[i + 1] == '/' ? 4 : 3;
                    code.append(c).append(s[i + 1]);
                    i += 2;
                    continue;
                }
                if (c == '?' && i + 1 < n && s[i + 1] == '>') {
                    inScript = false;
                    code.append(';');
                    list.add(new Content(code.toString(), true));
                    code = new StringBuilder();
                    i += 2;
                    continue;
                }
                code.append(c);
                i++;
            }
        }
        if (inScript) {
            list.add(new Content(code.toString(), true));
        } else {
            list.add(new Content(buffer.toString(), false));
        }
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
