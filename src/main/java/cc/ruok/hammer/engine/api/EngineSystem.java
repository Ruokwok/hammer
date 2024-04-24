package cc.ruok.hammer.engine.api;

import cc.ruok.hammer.Logger;
import cc.ruok.hammer.engine.Engine;
import cc.ruok.hammer.engine.Script;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.Part;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class EngineSystem {

    private final Engine engine;
    private final List<String> includeList = new ArrayList<>();
    private Map<String, EngineCookie> cookies;
    private ArrayList<EngineFile> uploads;
    private File partsDir;

    public EngineSystem(Engine engine) {
        this.engine = engine;
    }

    public long getTime() {
        return System.currentTimeMillis();
    }

    public void info(String str) {
        Logger.info(str);
    }

    public void warning(String str) {
        Logger.warning(str);
    }

    public void output(Object obj) {
        if (obj == null) {
            engine.outputStatic(null);
        } else {
            engine.outputStatic(obj.toString());
        }
    }

    public void outputScript(Object obj) {
        if (obj == null) {
            engine.outputScript(null);
        } else {
            if (obj instanceof String[] && ((String[]) obj).length == 1) {
                engine.outputScript(((String[]) obj)[0]);
            } else if (obj instanceof Collection<?> || obj instanceof Map<?,?> || obj instanceof Object[]) {
                engine.outputScript(JSONUtil.toJsonStr(obj));
            } else {
                engine.outputScript(obj.toString());
            }
        }
    }

    public String include(String str, String file) throws EngineException {
        if (includeList.contains(file)) {
            throw new EngineException("prohibit circular include.");
        } else {
            includeList.add(file);
            if (file.endsWith(".js")) return str;
            Script script = new Script(str, engine);
            return script.getCompile();
        }
    }

    public Map<String, Object> getSession(Object time) throws EngineException {
        if (time instanceof Integer || time instanceof Long) {
            engine.getSession().setMaxInactiveInterval((Integer) time);
            return engine.getSessionData();
        } else {
            throw new EngineException("the params must be of type int.");
        }
    }

    public void saveSession(Object obj) {
        if (obj instanceof Map<?,?>) {
            engine.saveSessionData((Map<String, Object>) obj);
        }
    }

    public void sessionClose() {
        engine.destroySession();
    }

    public void setStatus(Object o) throws EngineException {
        if (o instanceof Integer) {
            engine.getResponse().setStatus((Integer) o);
        } else {
            throw new EngineException("the status code must be of type int.");
        }
    }

    public Map<String, EngineCookie> getCookies() {
        if (cookies == null) {
            cookies = new HashMap<>();
            Cookie[] _cookie = engine.getRequest().getCookies();
            if (_cookie == null) return cookies;
            for (Cookie c : _cookie) {
                EngineCookie cookie = new EngineCookie();
                cookie.name = c.getName();
                cookie.value = c.getValue();
                cookie.path = c.getPath();
                cookie.domain = c.getDomain();
                cookie.age = c.getMaxAge();
                cookie.httpOnly = c.isHttpOnly();
                cookies.put(c.getName(), cookie);
            }
        }
        return cookies;
    }

    public void putCookie(String name, String value, String path, String domain, int maxAge, boolean httpOnly) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(httpOnly);
        engine.getResponse().addCookie(cookie);
    }

    public EngineFile getFile(String filename) {
        return new EngineFile(new File(engine.getWebSite().getPath() + "/" + filename), engine);
    }

    public void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            Logger.logException(e);
        }
    }

    public ArrayList<EngineFile> getUploadParts() throws ServletException, IOException {
        if (uploads != null) return uploads;
        if (engine.getRequest().getContentType() == null) return null;
        if (!engine.getRequest().getContentType().startsWith("multipart/form-data;")) return null;
        Collection<Part> parts = engine.getRequest().getParts();
        if (parts.size() == 0) return null;
        ArrayList<EngineFile> list = new ArrayList<>();
        String uuid = UUID.randomUUID().toString();
        for (Part part : parts) {
            File file = new File("temp/" + uuid + "/" + part.getSubmittedFileName());
            partsDir = new File("temp/" + uuid);
            partsDir.mkdir();
            FileOutputStream stream = new FileOutputStream(file);
            IOUtils.write(part.getInputStream().readAllBytes(), stream);
            stream.close();
            list.add(new EngineFile(file, engine));
        }
        uploads = list;
        return list;
    }

    public void removeParts() {
        if (partsDir != null) {
            try {
                FileUtils.forceDelete(partsDir);
            } catch (IOException e) {
                Logger.logException(e);
            }
        }
    }
}
