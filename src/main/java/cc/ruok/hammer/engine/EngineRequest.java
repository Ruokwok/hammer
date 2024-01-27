package cc.ruok.hammer.engine;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public abstract class EngineRequest {

    public static EngineRequest createEngineRequest(HttpServletRequest request) {
        return new EngineRequest() {
            @Override
            public String getMethod() {
                return request.getMethod();
            }

            @Override
            public String getAddress() {
                return request.getRemoteHost();
            }

            @Override
            public String getPath() {
                return request.getServletPath();
            }

            @Override
            public int getPort() {
                return request.getRemotePort();
            }

            @Override
            public String getProtocol() {
                return request.getProtocol();
            }

            @Override
            public String getHeader(String key) {
                return request.getHeader(key);
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> map = new HashMap<>();
                Enumeration<String> names = request.getHeaderNames();
                while (names.hasMoreElements()) {
                    String h = names.nextElement();
                    map.put(h, request.getHeader(h));
                }
                return map;
            }
        };
    }

    public abstract String getMethod();

    public abstract String getAddress();

    public abstract String getPath();

    public abstract int getPort();

    public abstract String getProtocol();

    public abstract String getHeader(String key);

    public abstract Map<String, String> getHeaders();
}
