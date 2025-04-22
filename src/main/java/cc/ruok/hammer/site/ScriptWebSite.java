package cc.ruok.hammer.site;

import cc.ruok.hammer.*;
import cc.ruok.hammer.engine.HttpEngine;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class ScriptWebSite extends WebSite {

    public HashMap<String, ComboPooledDataSource> pool = new HashMap<>();
    public Hashtable<String, Object> cache;

    public ScriptWebSite(Config config) {
        super(config);
    }

    @Override
    public void execute(File file, HttpServletRequest req, HttpServletResponse resp) throws IOException {
            long start = System.currentTimeMillis();
            String filter = filter(req.getServletPath());
            String extensions = getExtensions(file.getName());
            if (Hammer.config.scriptFileTypes.contains(extensions)) {
                String script = FileUtils.readFileToString(file, "utf-8");
                HttpEngine e = new HttpEngine(script, req, resp, this);
                WebServer.getInstance().putThreadSite(this);
                if (filter != null) e.setQueryUrl(filter);
                e.execute();
                WebServer.getInstance().removeThreadSite();
                long end = System.currentTimeMillis();
                Logger.info("[" + getName() + "][" + req.getMethod() + "][" + resp.getStatus() + "]" +
                        req.getRemoteAddr() +
                        " - " + req.getRequestURI() + "(" + (end - start) + "ms)");

            } else {
                FileInputStream inputStream = new FileInputStream(file);
                IOUtils.write(inputStream.readAllBytes(), resp.getOutputStream());
                Logger.info("[" + getName() + "][" + req.getMethod() + "][" + resp.getStatus() + "]" +
                        req.getRemoteAddr() +
                        " - " + req.getRequestURI());
                inputStream.close();
            }
    }

    public boolean getPermission(String key) {
        if (config.permission == null) return false;
        if (!config.permission.containsKey(key)) return false;
        return config.permission.get(key);
    }

    private String getExtensions(String filename) {
        //TODO 此方法待完善
        if (!filename.contains(".")) return null;
        String[] split = filename.split("\\.");
        return split[split.length - 1];
    }

    public void putCache(String key, Object value) {
        if (cache == null) cache = new Hashtable<>();
        cache.put(key, value);
    }

    public Object getCache(String key) {
        if (cache == null) return null;
        return cache.get(key);
    }

    public void removeCache(String key) {
        if (cache == null) return;
        cache.remove(key);
    }

    @Override
    public void enable() {
        super.enable();
        if (config.database_pool != null) {
            for (Map.Entry<String, Config.DatabasePool> entry : config.database_pool.entrySet()) {
                ComboPooledDataSource cpds = new ComboPooledDataSource();
                try {
                    cpds.setDriverClass("com.mysql.cj.jdbc.Driver");
                    cpds.setJdbcUrl("jdbc:" + entry.getValue().url);
                    cpds.setUser(entry.getValue().username);
                    cpds.setPassword(entry.getValue().password);
                    cpds.setIdleConnectionTestPeriod(entry.getValue().idle_conn_test);
                    cpds.setMaxIdleTime(entry.getValue().max_idle_time);
                    cpds.setMaxPoolSize(entry.getValue().max_size);
                    cpds.setInitialPoolSize(entry.getValue().init_size);
                    cpds.setMinPoolSize(entry.getValue().min_size);
                    cpds.setTestConnectionOnCheckout(true);
                    pool.put(entry.getKey(), cpds);
                } catch (PropertyVetoException e) {
                    Logger.logException(e);
                }
            }
        }
    }

    @Override
    public void disable() {
        for (Map.Entry<String, ComboPooledDataSource> entry : pool.entrySet()) {
            entry.getValue().close();
        }
        super.disable();
    }
}
