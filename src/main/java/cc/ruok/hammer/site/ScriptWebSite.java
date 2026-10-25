package cc.ruok.hammer.site;

import cc.ruok.hammer.*;
import cc.ruok.hammer.engine.CronTask;
import cc.ruok.hammer.engine.HttpEngine;
import cc.ruok.hammer.engine.Script;
import cc.ruok.hammer.engine.api.EngineException;
import cc.ruok.hammer.engine.api.EngineValues;
import cn.hutool.cron.CronUtil;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

public class ScriptWebSite extends WebSite {

    public HashMap<String, ComboPooledDataSource> pool = new HashMap<>();
    public Hashtable<String, Object> cache;
    private ArrayList<String> scheduleId;

    public ScriptWebSite(Config config) {
        super(config);
    }

    @Override
    public void execute(File file, HttpServletRequest req, HttpServletResponse resp) throws IOException {
            long start = System.currentTimeMillis();
            String filter = filter(req.getServletPath());
            String extensions = getExtensions(file.getName());
            boolean fullScript = Script.isFullScript(file.getName());
            if (Hammer.config.scriptFileTypes.contains(extensions) || fullScript) {
                String script = FileUtils.readFileToString(file, "utf-8");
                HttpEngine e = new HttpEngine(script, req, resp, this, fullScript);
                WebServer.getInstance().putThreadSite(this);
                if (filter != null) e.setQueryUrl(filter);
                e.execute();
                WebServer.getInstance().removeThreadSite();
                long end = System.currentTimeMillis();
                Logger.info("[" + getName() + "][" + req.getMethod() + "][" + resp.getStatus() + "]" +
                        req.getRemoteAddr() +
                        " - " + req.getRequestURI() + "(" + (end - start) + "ms)");

            } else {
                input(file, resp.getOutputStream());
                Logger.info("[" + getName() + "][" + req.getMethod() + "][" + resp.getStatus() + "]" +
                        req.getRemoteAddr() +
                        " - " + req.getRequestURI());
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
                Config.DatabasePool databasePool = entry.getValue();
                try {
                    cpds.setDriverClass("com.mysql.cj.jdbc.Driver");
                    cpds.setJdbcUrl("jdbc:" + databasePool.url);
                    cpds.setUser(databasePool.username);
                    cpds.setPassword(databasePool.password);
                    cpds.setIdleConnectionTestPeriod(databasePool.idle_conn_test);
                    cpds.setMaxIdleTime(databasePool.max_idle_time);
                    cpds.setMaxPoolSize(databasePool.max_size);
                    cpds.setInitialPoolSize(databasePool.init_size);
                    cpds.setMinPoolSize(databasePool.min_size);
                    cpds.setTestConnectionOnCheckout(true);
                    cpds.setTestConnectionOnCheckin(databasePool.test_connection_on_checkin);
                    cpds.setBreakAfterAcquireFailure(databasePool.break_after_acquire_failure);
                    if (databasePool.preferred_test_query != null && !databasePool.preferred_test_query.isBlank()) {
                        cpds.setPreferredTestQuery(databasePool.preferred_test_query);
                    }
                    if (databasePool.checkout_timeout > 0) {
                        cpds.setCheckoutTimeout(databasePool.checkout_timeout);
                    }
                    if (databasePool.acquire_retry_attempts >= 0) {
                        cpds.setAcquireRetryAttempts(databasePool.acquire_retry_attempts);
                    }
                    if (databasePool.acquire_retry_delay >= 0) {
                        cpds.setAcquireRetryDelay(databasePool.acquire_retry_delay);
                    }
                    if (databasePool.connection_is_valid_timeout > 0) {
                        cpds.setConnectionIsValidTimeout(databasePool.connection_is_valid_timeout);
                    }
                    if (databasePool.max_connection_age > 0) {
                        cpds.setMaxConnectionAge(databasePool.max_connection_age);
                    }
                    if (databasePool.jdbc_properties != null && !databasePool.jdbc_properties.isEmpty()) {
                        Properties properties = new Properties();
                        for (Map.Entry<String, Object> property : databasePool.jdbc_properties.entrySet()) {
                            if (property.getValue() != null) {
                                properties.setProperty(property.getKey(), String.valueOf(property.getValue()));
                            }
                        }
                        cpds.setProperties(properties);
                    }
                    pool.put(entry.getKey(), cpds);
                } catch (PropertyVetoException e) {
                    Logger.logException(e);
                }
            }
        }
        if (config.tasks != null) {
            scheduleId = new ArrayList<>();
            for (Map.Entry<String, Config.Task> entry : config.tasks.entrySet()) {
                Config.Task task = entry.getValue();
                try {
                    String sid = CronUtil.schedule(task.cron, new CronTask(task.script, this));
                    scheduleId.add(sid);
                } catch (EngineException e) {
                    Logger.logException(e);
                }
            }
        }
    }

    public boolean hasCron() {
        return scheduleId != null;
    }

    @Override
    public void disable() {
        for (Map.Entry<String, ComboPooledDataSource> entry : pool.entrySet()) {
            entry.getValue().close();
        }
        if (scheduleId != null) {
            for (String sid : scheduleId) {
                CronUtil.remove(sid);
            }
        }
        super.disable();
    }

    public HashMap<String, Object> getValues() {
        return config.values;
    }
}
