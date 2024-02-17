package cc.ruok.hammer.engine;

import cn.hutool.http.HttpUtil;

import java.util.Map;

public class EngineHttp {

    public String get(String url, int timeout) throws EngineException {
        try {
            return HttpUtil.get(url, timeout);
        } catch (Exception e) {
            throw new EngineException(e.getMessage());
        }
    }

    public String get(String url) throws EngineException {
        return get(url, 5000);
    }

    public String post(String url, Map<String, Object> params, int timeout) throws EngineException {
        try {
            return HttpUtil.post(url, params, timeout);
        } catch (Exception e) {
            throw new EngineException(e.getMessage());
        }
    }

    public String post(String url, Map<String, Object> params) throws EngineException {
        return post(url, params, 5000);
    }

    public String post(String url, String data, int timeout) throws EngineException {
        try {
            return HttpUtil.post(url, data, timeout);
        } catch (Exception e) {
            throw new EngineException(e.getMessage());
        }
    }

    public String post(String url, String data) throws EngineException {
        return post(url, data, 5000);
    }

}