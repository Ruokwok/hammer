package cc.ruok.hammer.engine;

import cc.ruok.hammer.Logger;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.graalvm.polyglot.PolyglotException;

import javax.script.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Engine {

    private String script;
    private LinkedList<Content> list = new LinkedList<>();
    private ScriptEngineManager manager = new ScriptEngineManager();
    private ScriptEngine engine = manager.getEngineByName("graal.js");
    private EngineRequest request;
    private static String baseJs;
    private int i = 0;
    private PrintWriter writer;

    public Engine(String script, EngineRequest request, HttpServletRequest hsr, PrintWriter writer) {
        this.script = script;
        this.request = request;
        this.writer = writer;
        analysis();
        try {
            engine.put("System", new EngineSystem(this));
            engine.put("Request", request);
            if (hsr != null) {
//                engine.put("_PARAMS", hsr.getParameterMap());
                engine.put("_GET", getParams(hsr.getQueryString()));
                engine.put("_POST", getParams(getPostData(hsr)));
            }

            engine.eval(baseJs);
        } catch (ScriptException e) {
            Logger.logException(e);
        }
    }

    public Engine(String script) {
        this(script, null, null, null);
    }

    public void analysis() {
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
//        for (Content content : list) {
//            if (content.isScript()) System.out.println(content);
//        }
    }

    public String execute() {
        String sb = "";
        try {
            String compile = compile();
            engine.eval(compile);
        } catch (ScriptException e) {
            error(e);
        }
        System.gc();
        return sb;
    }

    public String compile() {
        StringBuilder sb = new StringBuilder();
        for (Content content : list) {
            if (content.isScript()) {
                sb.append(content);
            } else {
                int line = content.getLine();
                sb.append("System.output();");
                sb.append("\n".repeat(Math.max(0, line - 1)));
            }
        }
        return sb.toString();
    }

    public void error(ScriptException e) {
        Throwable cause = e.getCause();
        String msg = cause.getMessage().replaceAll("System\\.output\\(\\);", "");
        if (cause instanceof PolyglotException) {
            PolyglotException pe = (PolyglotException) cause;
            int line = pe.getSourceLocation().getStartLine();
            msg += " (on line " + line + ")";
        }
        output("<p style='color:red'><strong>Error: </strong>" + msg + "</p>");
    }

    public Map<String, String[]> getParams(String url) {
        Map<String, String[]> map = new HashMap<>();
        if (url == null) return map;
        String[] split = url.split("&");
        for (String s : split) {
            if (s.contains("=")) {
                String[] _s = s.split("=");
                if (map.containsKey(_s[0])) {
                    String[] values = map.get(_s[0]);
                    List<String> l = new ArrayList<>(Arrays.asList(values));
                    l.add(_s[1]);
                    map.put(_s[0], l.toArray(new String[values.length + 1]));
                } else {
                    map.put(_s[0], new String[]{_s[1]});
                }
            }
        }
        return map;
    }

    private String getPostData(HttpServletRequest request) {
        StringBuffer data = new StringBuffer();
        String line = null;
        BufferedReader reader = null;
        try {
            reader = request.getReader();
            while (null != (line = reader.readLine()))
                data.append(line);
        } catch (IOException e) {
        }
        return data.toString();
    }

    protected void output() {
        while (list.get(i).isScript()) {
            i++;
        }
        writer.print(list.get(i));
        i++;
    }

    protected void output(String str) {
        writer.print(str);
    }

    public static void loadBaseJs() {
        try {
            baseJs = IOUtils.toString(Engine.class.getResourceAsStream("/engine.js"), "utf8");
        } catch (IOException e) {
            Logger.logException(e);
        }
    }

}
