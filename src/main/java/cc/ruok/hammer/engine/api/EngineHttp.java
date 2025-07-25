package cc.ruok.hammer.engine.api;

import cc.ruok.hammer.engine.Engine;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EngineHttp extends EngineAPI {

    public EngineHttp(Engine engine) {
        super(engine);
    }

    public Response get(String url, Map<String, String> header, Map<String, String> cookies, int timeout) throws EngineException {
        try {
            HttpRequest request = HttpRequest.get(url);
            request.setConnectionTimeout(timeout);
            request.setReadTimeout(timeout);
            if (header != null && header.size() > 0) {
                for (Map.Entry<String, String> entry: header.entrySet()) {
                    request.header(entry.getKey(), entry.getValue());
                }
            }
            if (cookies != null && cookies.size() > 0) {
                for (Map.Entry<String, String> entry: cookies.entrySet()) {
                    HttpCookie cookie = new HttpCookie(entry.getKey(), entry.getValue());
                    request.cookie(cookie);
                }
            }
            return new Response(request.execute(), engine);
        } catch (Exception e) {
            throw new EngineException(e);
        }
    }

    public Response get(String url, Map<String, String> header, Map<String, String> cookies) throws EngineException {
        return get(url, header, cookies, 5000);
    }

    public Response get(String url, Map<String, String> header) throws EngineException {
        return get(url, header,null, 5000);
    }

    public Response get(String url, int time) throws EngineException {
        return get(url, null, null, time);
    }

    public Response get(String url) throws EngineException {
        return get(url, null, null, 5000);
    }

    public Response post(String url, String data, Map<String, Object> params, Map<String, String> header, Map<String, String> cookies, int timeout) throws EngineException {
        try {
            HttpRequest request = HttpRequest.post(url);
            request.setConnectionTimeout(timeout);
            request.body(data);
            if (params != null) request.form(params);
            if (header != null && header.size() > 0) {
                for (Map.Entry<String, String> entry: header.entrySet()) {
                    request.header(entry.getKey(), entry.getValue());
                }
            }
            if (cookies != null && cookies.size() > 0) {
                for (Map.Entry<String, String> entry: cookies.entrySet()) {
                    HttpCookie cookie = new HttpCookie(entry.getKey(), entry.getValue());
                    request.cookie(cookie);
                }
            }
            return new Response(request.execute(), engine);
        } catch (Exception e) {
            throw new EngineException(e);
        }
    }

    public Response post(String url, String data, Map<String, Object> params, Map<String, String> header, Map<String, String> cookies) throws EngineException {
        return post(url, data, params, header, cookies, 5000);
    }

    public Response post(String url, Map<String, Object> params, Map<String, String> header) throws EngineException {
        return post(url, null, params, header, null, 5000);
    }

    public Response post(String url, Map<String, Object> params) throws EngineException {
        return post(url, null, params, null, null, 5000);
    }

    public Response post(String url, String data, Map<String, String> header, Map<String, String> cookies, int timeout) throws EngineException {
        try {
            HttpRequest request = HttpRequest.post(url);
            request.setConnectionTimeout(timeout);
            request.body(data);
            if (header != null && header.size() > 0) {
                for (Map.Entry<String, String> entry: header.entrySet()) {
                    request.header(entry.getKey(), entry.getValue());
                }
            }
            if (cookies != null && cookies.size() > 0) {
                for (Map.Entry<String, String> entry: cookies.entrySet()) {
                    HttpCookie cookie = new HttpCookie(entry.getKey(), entry.getValue());
                    request.cookie(cookie);
                }
            }
            return new Response(request.execute(), engine);
        } catch (Exception e) {
            throw new EngineException(e);
        }
    }

    public Response post(String url, String data, Map<String, String> map, Map<String, String> cookies) throws EngineException {
        return post(url, data, map, cookies, 5000);
    }

    public Response post(String url, String data, Map<String, String> map) throws EngineException {
        return post(url, data, map, null, 5000);
    }

    public Response post(String url, String data) throws EngineException {
        return post(url, data, null, null, 5000);
    }

    public static EngineCookie toEngineCookie(HttpCookie cookie) {
        EngineCookie ec = new EngineCookie();
        ec.name = cookie.getName();
        ec.value = cookie.getValue();
        ec.httpOnly = cookie.isHttpOnly();
        ec.age = (int) cookie.getMaxAge();
        ec.path = cookie.getPath();
        ec.domain = cookie.getDomain();
        return ec;
    }

    public static HttpCookie toHttpCookie(EngineCookie cookie) {
        HttpCookie ec = new HttpCookie(cookie.name, cookie.value);
        ec.setHttpOnly(cookie.httpOnly);
        ec.setMaxAge(cookie.getMaxAge());
        ec.setPath(cookie.path);
        ec.setDomain(cookie.domain);
        return ec;
    }

    public Request request(String url) {
        return new Request(url, engine);
    }

    @Override
    public String getVarName() {
        return "Http";
    }

    public static class Response {

        private HttpResponse response;
        private Engine engine;

        public Response(HttpResponse response, Engine engine) {
            this.response = response;
            this.engine = engine;
        }

        public int code() {
            return response.getStatus();
        }

        public String body() {
            return response.body();
        }

        public EngineData data() {
            return new EngineData(response.bodyBytes(), engine);
        }

        public String header(String name) {
            return response.header(name);
        }

        public Map<String, List<String>> headers() {
            return response.headers();
        }

        public List<EngineCookie> cookies() {
            List<HttpCookie> cookies = response.getCookies();
            if (cookies != null && cookies.size() > 0) {
                List<EngineCookie> list = new ArrayList<>();
                for (HttpCookie cookie : cookies) {
                    EngineCookie ec = toEngineCookie(cookie);
                    list.add(ec);
                }
                return list;
            }
            return null;
        }

        public EngineCookie cookie(String name) {
            HttpCookie cookie = response.getCookie(name);
            if (cookie == null) return null;
            return toEngineCookie(cookie);
        }

        @Override
        public String toString() {
            return body();
        }
    }

    public static class Request {

        private Engine engine;
        private int timeout = 5000;
        private Map<String, Object> params;
        private Map<String, String> header;
        private Map<String, String> cookies;
        private String body;
        private HttpRequest request;

        public Request(String url, Engine engine) {
            this.engine = engine;
            this.request = HttpRequest.get(url);
        }

        public void setMethod(String method) {
            request.setMethod(Method.valueOf( method));
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        public void setParams(Map<String, Object> params) {
            this.params = params;
        }

        public void setHeader(Map<String, String> header) {
            this.header = header;
        }

        public void setCookies(Map<String, String> cookies) {
            this.cookies = cookies;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public void setUrl(String url) {
            this.request.setUrl(url);
        }

        public Response exec() throws EngineException {
            try {
                request.setConnectionTimeout(timeout);
                request.body(body);
                if (params != null) request.form(params);
                if (header != null && header.size() > 0) {
                    for (Map.Entry<String, String> entry: header.entrySet()) {
                        request.header(entry.getKey(), entry.getValue());
                    }
                }
                if (cookies != null && cookies.size() > 0) {
                    for (Map.Entry<String, String> entry: cookies.entrySet()) {
                        HttpCookie cookie = new HttpCookie(entry.getKey(), entry.getValue());
                        request.cookie(cookie);
                    }
                }
                return new Response(request.execute(), engine);
            } catch (Exception e) {
                throw new EngineException(e);
            }
        }

    }
}