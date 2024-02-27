package cc.ruok.hammer.engine;

import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class EngineRequest {

    private final HttpServletRequest request;
    private final UserAgent ua;

    public EngineRequest(HttpServletRequest request) {
        this.request = request;
        this.ua = UserAgentUtil.parse(request.getHeader("User-Agent"));
    }

    public String getMethod() {
        return request.getMethod();
    }


    public String getAddress() {
        return request.getRemoteHost();
    }


    public String getPath() {
        return request.getServletPath();
    }


    public int getPort() {
        return request.getRemotePort();
    }


    public String getProtocol() {
        return request.getProtocol();
    }


    public String getHeader(String key) {
        return request.getHeader(key);
    }


    public String getDomain() {
        return request.getHeader("Host");
    }


    public Map<String, String> getHeaders() {
        Map<String, String> map = new HashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String h = names.nextElement();
            map.put(h, request.getHeader(h));
        }
        return map;
    }

    public String getBrowser() {
        if (ua != null) return ua.getBrowser().toString();
        return null;
    }

    public String getBrowserVersion() {
        if (ua != null) return ua.getVersion();
        return null;
    }

    public String getOS() {
        if (ua != null) return ua.getOs().toString();
        return null;
    }

    public boolean isMobile() {
        if (ua != null) return ua.isMobile();
        return false;
    }

    public String getEngine() {
        if (ua != null) return ua.getEngine().toString();
        return null;
    }

    public String getEngineVersion() {
        if (ua != null) return ua.getEngineVersion();
        return null;
    }
}
