package cc.ruok.hammer.engine;

import cc.ruok.hammer.Logger;
import cc.ruok.hammer.engine.api.*;
import cc.ruok.hammer.engine.task.NullWriter;
import cc.ruok.hammer.site.ScriptWebSite;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.io.IOUtils;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.SourceSection;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HttpEngine extends Engine {

    private EngineRequest request;
    private HttpServletRequest req;
    private HttpServletResponse response;
    private HttpSession session;

    public HttpEngine(String str, HttpServletRequest req, HttpServletResponse resp, ScriptWebSite webSite) throws IOException {
        super(str, req.getQueryString(), webSite);
        this.request = new EngineRequest(req);
        this.writer = resp.getWriter();
        this.response = resp;
        this.req = req;
        this.session = req.getSession();
        this.system = new EngineSystemHttp(this);
        try {
            engine.getBindings("js").putMember("Request", request);
            putObject(system);
            engine.getBindings("js").putMember("_GET", getParams(req.getQueryString()));
            engine.getBindings("js").putMember("_POST", getParams(getPostData(req)));
            String base = IOUtils.toString(HttpEngine.class.getResourceAsStream("/http.js"), "utf8");
            engine.eval("js", base);
        } catch (Exception e) {
            Logger.logException(e);
        }
    }

    public HttpEngine(String script) throws IOException {
        this(script, null, null, null);
    }

    @Override
    public void execute() {
        super.execute();
        engine.eval("js", "System.saveSession(_SESSION);");
    }

    public void finish() {
        closeAllConnect();
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

    @Override
    public void setStatus(int code) {
        response.setStatus(code);
    }

}
