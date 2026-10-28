package cc.ruok.hammer.engine;

import cc.ruok.hammer.Logger;
import cc.ruok.hammer.engine.api.Closeable;
import cc.ruok.hammer.engine.api.EngineAPI;
import cc.ruok.hammer.engine.api.EngineException;
import cc.ruok.hammer.engine.api.EngineSystem;
import cc.ruok.hammer.site.ScriptWebSite;
import cn.hutool.http.HtmlUtil;
import org.apache.commons.io.IOUtils;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.SourceSection;
import org.graalvm.polyglot.Value;

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
    protected static Source baseSource;
    protected EngineSystem system;
    private ContextPool.Lease contextLease;
    private boolean wrapScript;
    protected ScriptWebSite webSite;
    private static HashMap<String, Object> apiMap = new HashMap<>();
    private final List<Closeable> closeable = new ArrayList<>();
    private boolean running = false;
    private Object entry;
    private boolean consoleException = false;

    public Engine(String str, String url, PrintWriter writer, ScriptWebSite webSite, Object entry) {
        this(str, url, writer, webSite, entry, false);
    }

    public Engine(String str, String url, PrintWriter writer, ScriptWebSite webSite, Object entry, boolean fullScript) {
        this.str = str;
        this.url = url;
        this.webSite = webSite;
        this.script = new Script(str, this, fullScript);
        this.writer = writer;
        this.entry = entry;
        this.wrapScript = !usesInclude(str);
        try {
            contextLease = ContextPool.getInstance().acquire();
            engine = contextLease.getContext();
            resetContext();
        } catch (Exception e) {
            Logger.logException(e);
            releaseContext(false);
        }
    }

    private static boolean usesInclude(String str) {
        int index = str.indexOf("include");
        while (index >= 0) {
            int next = index + "include".length();
            while (next < str.length() && Character.isWhitespace(str.charAt(next))) next++;
            if (next < str.length() && str.charAt(next) == '(') return true;
            index = str.indexOf("include", index + "include".length());
        }
        return false;
    }

    private void resetContext() {
        Value bindings = engine.getBindings("js");
        for (String key : new String[]{"_ENTRY", "Request", "_POST", "System"}) {
            try {
                if (bindings.hasMember(key)) bindings.removeMember(key);
            } catch (Exception ignored) {
            }
        }
        bindings.putMember("_GET", getParams(url));
        if (baseSource != null) {
            engine.eval(baseSource);
        } else {
            engine.eval("js", baseJs);
        }
        engine.eval("js", "var _COOKIE = undefined; var _SESSION = undefined;");
    }

    public Engine(String str, String url, PrintWriter writer, ScriptWebSite webSite) {
        this(str, url, writer, webSite, null);
    }

    public void setRT(EngineSystem system) {
        this.system = system;
        engine.getBindings("js").putMember("System", system);
    }

    public void execute() {
        if (contextLease == null || engine == null) return;
        running = true;
        Set<String> before = null;
        if (wrapScript && !contextLease.isClosed()) {
            try {
                before = new HashSet<>(engine.getBindings("js").getMemberKeys());
            } catch (Exception e) {
                Logger.logException(e);
            }
        }
        try {
            String compile = script.getCompile();
            if (entry != null) {
                engine.getBindings("js").putMember("_ENTRY", entry);
            }
            if (wrapScript) {
                engine.eval("js", "(function(){" + compile + "\n}).call(globalThis);");
            } else {
                engine.eval("js", compile);
            }
        } catch (PolyglotException e) {
            if (running) error(e);
        } catch (IllegalStateException e) {
            running = false;
        } finally {
            try {
                if (running) {
                    this.finish();
                    running = false;
                }
            } finally {
                releaseContext(wrapScript && !isContextDirty(before));
            }
        }
    }

    private boolean isContextDirty(Set<String> before) {
        if (before == null || contextLease == null || contextLease.isClosed()) return true;
        try {
            return !before.equals(new HashSet<>(engine.getBindings("js").getMemberKeys()));
        } catch (Exception e) {
            return true;
        }
    }

    private void releaseContext(boolean reusable) {
        if (contextLease == null) return;
        contextLease.release(reusable);
        contextLease = null;
    }

    public void finish() {
        closeAllConnect();
        system.finish();
    }

    public void error(PolyglotException e) {
        if (consoleException) Logger.logException(e);
        String message = e.getMessage();
        StringBuilder msg = new StringBuilder(message == null ? "" : message.replaceAll("System\\.output\\('........'\\);", ""));
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
        outputScript("<p style='color:red'><strong>Error: </strong>" + HtmlUtil.escape(msg.toString()) + "</p>");
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
        if (str != null && !str.isEmpty()) {
            if (Character.isWhitespace(str.charAt(str.length() - 1))) {
                writer.print(str.substring(0, str.length() - 1));
            } else {
                writer.print(str);
            }
        }
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
        if (contextLease != null) contextLease.closeContext();
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
            baseSource = Source.create("js", baseJs);
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
