package cc.ruok.hammer.engine;

import cc.ruok.hammer.Logger;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;

import javax.script.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;

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
            if (hsr != null) engine.put("GET", hsr.getParameterMap());
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
        StringBuilder sb = new StringBuilder();

        try {
            String compile = compile();
            engine.eval(compile);
        } catch (ScriptException e) {
            String em = e.getMessage().replaceAll("System\\.output\\(\\);", "");
            output("<p style='color:red'><strong>Error:</strong>" +
                    StrUtil.sub(em, em.indexOf(":") + 1, -1) +
                    "</p>");
        }

//        for (Content content : list) {
//            if (content.isScript()) {
//                try {
//                    engine.eval(content.toString());
//                    List<Object> data = printData.getData();
//                    if (data != null) {
//                        for (Object obj : data) {
//                            sb.append(obj.toString());
//                        }
//                    }
//                    printData.clear();
//                } catch (ScriptException e) {
//                    Logger.logException(e);
//                    String em = e.getMessage();
//                    String msg = StrUtil.sub(em, em.indexOf(":") + 1, -1);
//                    sb.append("<script>alert('The script encountered an error, please check the detailed information in the console.')</script>").
//                            append("<script>console.error('").append(msg.replaceAll("[\r\n]", "\\\\n")).append("');</script>");
//                }
//            } else {
//                sb.append(content);
//            }
//        }
        System.gc();
        return sb.toString();
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
