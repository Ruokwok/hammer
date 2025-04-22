package cc.ruok.hammer.engine.api;

import cc.ruok.hammer.engine.Engine;
import cc.ruok.hammer.engine.HttpEngine;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.Part;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class EngineSystemHttp extends EngineSystem {

    private HttpEngine engine;
    private Map<String, EngineCookie> cookies;
    private ArrayList<EngineFile> uploads;
    private File partsDir;

    public EngineSystemHttp(Engine engine) {
        super(engine);
        if (engine instanceof HttpEngine he) {
            this.engine = he;
        } else {
            throw new RuntimeException("EngineSystemHttp can only be used with HttpEngine.");
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

    public ArrayList<EngineFile> getUploadParts(String name) throws ServletException, IOException {
        if (uploads != null) return uploads;
        if (engine.getRequest().getContentType() == null) return null;
        if (!engine.getRequest().getContentType().startsWith("multipart/form-data;")) return null;
        Collection<Part> parts = engine.getRequest().getParts();
        if (parts.size() == 0) return null;
        ArrayList<EngineFile> list = new ArrayList<>();
        String uuid = UUID.randomUUID().toString();
        for (Part part : parts) {
            if (name == null || name.equals(part.getName())) {
                if (part.getSubmittedFileName().isEmpty()) break;
                File file = new File("temp/" + uuid + "/" + part.getSubmittedFileName());
                partsDir = new File("temp/" + uuid);
                partsDir.mkdir();
                FileOutputStream stream = new FileOutputStream(file);
                IOUtils.write(part.getInputStream().readAllBytes(), stream);
                stream.close();
                list.add(new EngineFile(file, engine));
            }
        }
        uploads = list;
        return list;
    }

    public void removeParts() {
        if (partsDir != null && partsDir.exists()) {
            try {
                FileUtils.forceDelete(partsDir);
            } catch (IOException e) {
//                Logger.logException(e);
            }
        }
    }

    public void addHeader(String key, String value) {
        engine.getResponse().setHeader(key, value);
    }

    @Override
    public void finish() {
        super.finish();
        removeParts();
    }
}
