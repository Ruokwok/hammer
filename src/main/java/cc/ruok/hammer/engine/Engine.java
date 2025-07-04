package cc.ruok.hammer.engine;

import cc.ruok.hammer.Logger;
import cc.ruok.hammer.engine.api.Closeable;
import cc.ruok.hammer.engine.api.EngineAPI;
import cc.ruok.hammer.engine.api.EngineException;
import cc.ruok.hammer.engine.api.EngineSystem;
import cc.ruok.hammer.site.ScriptWebSite;
import org.apache.commons.io.IOUtils;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.SourceSection;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

public class Engine {

    protected int status = 200;
    protected String url;
    protected Context engine;
    protected String str;
    protected Script script;
    protected PrintWriter writer;
    protected Map<String, String> outputPool = new HashMap<>();
    protected static String baseJs;
    protected EngineSystem system;
    protected ScriptWebSite webSite;
    private static HashMap<String, Object> apiMap = new HashMap<>();
    private final List<Closeable> closeable = new ArrayList<>();
    private boolean running = false;
    private Object entry;
    private boolean consoleException = false;

    public Engine(String str, String url, PrintWriter writer, ScriptWebSite webSite, Object entry) {
        this.str = str;
        this.url = url;
        this.webSite = webSite;
        this.script = new Script(str, this);
        this.writer = writer;
        this.entry = entry;
        try {
            engine = Context.newBuilder("js").allowAllAccess(true).build();
            engine.getBindings("js").putMember("_GET", getParams(url));
            engine.eval("js", baseJs);
        } catch (Exception e) {
            Logger.logException(e);
        }
    }

    public Engine(String str, String url, PrintWriter writer, ScriptWebSite webSite) {
        this(str, url, writer, webSite, null);
    }

    public void setRT(EngineSystem system) {
        this.system = system;
        engine.getBindings("js").putMember("System", system);
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
        running = true;
        try {
            String compile = script.getCompile();
            if (entry != null) {
                engine.getBindings("js").putMember("_ENTRY", entry);
            }
            engine.eval("js", compile);
        } catch (PolyglotException e) {
            if (running) error(e);
        } catch (IllegalStateException e) {
            running = false;
        } finally {
            if (running) {
                finish();
                running = false;
            }
        }
    }

    public void finish() {
        closeAllConnect();
        system.finish();
    }

    public void error(PolyglotException e) {
        if (consoleException) Logger.logException(e);
        StringBuilder msg = new StringBuilder(e.getMessage().replaceAll("System\\.output\\('........'\\);", ""));
        int line = 0;
        SourceSection location = e.getSourceLocation();
        if (location != null) line = location.getStartLine();
        if (line == 0) {
            StackTraceElement[] stackTrace = e.getStackTrace();
            for (StackTraceElement st : stackTrace) {
                if (st.getClassName().equals("<js>")) {
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

    public Context getContext() {
        return engine;
    }
    public void addCloseable(Closeable closeable) {
        this.closeable.add(closeable);
    }

    public void closeAllConnect() {
        for (Closeable closeable : closeable) {
            try {
                if (!closeable.isKeep()) {
                    closeable.close();
                }
            } catch (EngineException e) {
            }
        }
    }

    public Map<String, Object> getParams(String url) {
        Map<String, Object> map = new HashMap<>();
        if (url == null) return map;
        url = url.substring(url.indexOf("?") + 1);
        String[] split = url.split("&");
        for (String s : split) {
            if (s.contains("=")) {
                String[] pair = s.split("=", 2);
                String key = pair[0];
                String value = pair[1];
                if (map.containsKey(key)) {
                    Object currentValue = map.get(key);
                    if (currentValue instanceof String) {
                        List<String> list = new ArrayList<>(Arrays.asList((String) currentValue, value));
                        map.put(key, list.toArray(new String[0]));
                    } else if (currentValue instanceof String[]) {
                        String[] values = (String[]) currentValue;
                        List<String> list = new ArrayList<>(Arrays.asList(values));
                        list.add(value);
                        map.put(key, list.toArray(new String[0]));
                    }
                } else {
                    map.put(key, value);
                }
            }
        }
        return map;
    }

    public ScriptWebSite getWebSite() {
        return webSite;
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

    public void putOutput(String key, String value) {
        outputPool.put(key, value);
    }

    public void putObject(EngineAPI api) {
        engine.getBindings("js").putMember(api.getVarName() , api);
    }

    public void setStatus(int code) {
        this.status = code;
    }

    public void setQueryUrl(String url) {
        Map<String, Object> params = getParams(url.substring(url.indexOf("?") + 1));
        if (params.isEmpty()) return;
        engine.getBindings("js").putMember("_GET", params);
    }

    public int getStatus() {
        return status;
    }

    public void close(int code) {
        if (code >= 0) setStatus(code);
        running = false;
        engine.close();
    }

    public void close() {
        this.close(-1);
    }

    public void setConsoleException(boolean b) {
        this.consoleException = b;
    }

    public static void registerAPI(String var, Object object) {
        apiMap.put(var, object);
    }
    public static Object getModule(String name) {
        return apiMap.get(name);
    }

    public static Map<String, Object> getApiMap() {
        return apiMap;
    }

    public static void loadBaseJs() {
        try {
            baseJs = IOUtils.toString(HttpEngine.class.getResourceAsStream("/engine.js"), "utf8");
        } catch (IOException e) {
            Logger.logException(e);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public static class NullWriter extends PrintWriter {

        public NullWriter() {
            super(OutputStream.nullOutputStream());
        }

        @Override
        public void write(char[] cbuf, int off, int len) {
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    }
}
