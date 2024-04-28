package cc.ruok.hammer.engine;

import cc.ruok.hammer.Logger;
import cc.ruok.hammer.engine.api.*;
import cc.ruok.hammer.site.ScriptWebSite;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.io.IOUtils;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.SourceSection;

import javax.script.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Engine {

    private String str;
    private Script script;
    private Map<String, String> outputPool = new HashMap<>();
    private Context engine;
    private EngineRequest request;
    private HttpServletRequest req;
    private static String baseJs;
    private static String finishJs;
    private int i = 0;
    private PrintWriter writer;
    private HttpServletResponse response;
    private ScriptWebSite webSite;
    private HttpSession session;
    private EngineDatabase database = new EngineDatabase();

    public Engine(String str, HttpServletRequest req, HttpServletResponse resp, ScriptWebSite webSite) throws IOException {
        this.str = str;
        this.script = new Script(str, this);
        this.request = new EngineRequest(req);
        this.writer = resp.getWriter();
        this.response = resp;
        this.webSite = webSite;
        this.req = req;
        this.session = req.getSession();
        try {
            engine = Context.newBuilder("js").allowAllAccess(true).build();
            engine.getBindings("js").putMember("System", new EngineSystem(this));
            engine.getBindings("js").putMember("Request", request);
            engine.getBindings("js").putMember("Date", new EngineDate(this));
            engine.getBindings("js").putMember("Http", new EngineHttp());
            engine.getBindings("js").putMember("Digest", new EngineDigester());
            engine.getBindings("js").putMember("Database", database);
            engine.getBindings("js").putMember("Codec", new EngineCodec());
            if (req != null) {
                engine.getBindings("js").putMember("_GET", getParams(req.getQueryString()));
                engine.getBindings("js").putMember("_POST", getParams(getPostData(req)));
            }
            engine.eval("js", baseJs);
        } catch (Exception e) {
            Logger.logException(e);
        }
    }

    public Engine(String script) throws IOException {
        this(script, null, null, null);
    }

    public List<Content> analysis() {
        return analysis(str);
    }

    public List<Content> analysis(String script) {
        LinkedList<Content> list = new LinkedList<>();
        if (!script.contains("<?hsp")) {
            list.add(new Content(script, false));
            return list;
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
        return list;
//        for (Content content : list) {
//            if (content.isScript()) System.out.println(content);
//        }
    }

    public void execute() {
        try {
            String compile = script.getCompile();
            engine.eval("js", compile);
            engine.eval("js", finishJs);
            database.closeAll();
        } catch (PolyglotException e) {
            error(e);
        }
        System.gc();
    }

    public void error(PolyglotException e) {
        StringBuilder msg = new StringBuilder(e.getMessage().replaceAll("System\\.output\\('........'\\);", ""));
        int line = 0;
        SourceSection location = e.getSourceLocation();
        if (location != null) line = location.getStartLine();
        if (line == 0) {
            StackTraceElement[] stackTrace = e.getStackTrace();
            for (StackTraceElement st : stackTrace) {
                if (st.getClassName().equals("<js>") && st.getMethodName().equals(":program")) {
                    line = st.getLineNumber();
                    break;
                }
            }
        }
        if (line == 0) {
            StackTraceElement[] stackTrace = e.getStackTrace();
            for (StackTraceElement st : stackTrace) {
                if (st.getClassName().equals("<js>") && st.getMethodName().equals(":program")) {
                    line = st.getLineNumber();
                    break;
                }
            }
        }
        if (line > 0) {
            msg.append(" (on line ").append(line).append(")");
        }
        outputScript("<p style='color:red'><strong>Error: </strong>" + msg + "</p>");
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

    public ScriptWebSite getWebSite() {
        return webSite;
    }

    public Map<String, Object> getSessionData() {
        Enumeration<String> names = session.getAttributeNames();
        Map<String, Object> map = new ConcurrentHashMap<>();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            map.put(name, session.getAttribute(name));
        }
        return map;
    }

    public void saveSessionData(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                session.setAttribute(entry.getKey(), entry.getValue());
            }
        }
    }

    public void destroySession() {
        session.invalidate();
    }

    public HttpSession getSession() {
        return session;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public HttpServletRequest getRequest() {
        return req;
    }

    private String getPostData(HttpServletRequest request) {
        if (request.getContentType() == null) return null;
        if (request.getContentType().startsWith("multipart/form-data;")) return null;
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

    public void putOutput(String key, String value) {
        outputPool.put(key, value);
    }

    public void outputStatic(String key) {
        String value = outputPool.get(key);
        if (value != null) {
            outputScript(value);
        }
    }

    public void outputScript(String str) {
        writer.print(str);
    }

    public static void loadBaseJs() {
        try {
            baseJs = IOUtils.toString(Engine.class.getResourceAsStream("/engine.js"), "utf8");
            finishJs = IOUtils.toString(Engine.class.getResourceAsStream("/finish.js"), "utf8");
        } catch (IOException e) {
            Logger.logException(e);
        }
    }

}
