package cc.ruok.hammer.site;

import cc.ruok.hammer.*;
import cc.ruok.hammer.engine.Engine;
import cc.ruok.hammer.error.Http403Exception;
import cc.ruok.hammer.error.Http404Exception;
import cc.ruok.hammer.error.Http500Exception;
import cc.ruok.hammer.error.HttpException;
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
import java.util.Map;

public class ScriptWebSite extends WebSite {

    public HashMap<String, PseudoStatic> pseudoStaticMap = new HashMap<>();
    public HashMap<String, ComboPooledDataSource> pool = new HashMap<>();

    public ScriptWebSite(Config config) {
        super(config);
        if (config.pseudo_static != null && config.pseudo_static.size() > 0) {
            for (String exp : config.pseudo_static) {
                PseudoStatic pseudoStatic = new PseudoStatic(exp);
                if (pseudoStatic.isValid()) pseudoStaticMap.put(pseudoStatic.getOrigin(), pseudoStatic);
            }
        }
    }

    @Override
    public void handler(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Logger.info("[" + getName() + "][" + req.getMethod() + "][" + resp.getStatus() + "]" +
                    req.getRemoteAddr() +
                    " - " + req.getRequestURI());
            long start = System.currentTimeMillis();
            resp.setCharacterEncoding("utf8");
            String filter = filter(req.getServletPath());
            String filePath;
            if (filter == null) {
                filePath = req.getServletPath();
            } else {
                if (filter.contains("?")) {
                    filePath = filter.substring(0, filter.indexOf("?"));
                } else {
                    filePath = filter;
                }
            }
            File file = getFile(filePath);
            String type = WebServlet.getFileType(file.getName());
            resp.setHeader("Content-Type", type);
            if (type.equals("application/octet-stream")) {
                resp.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
            }
            String extensions = getExtensions(file.getName());
            if (Hammer.config.scriptFileTypes.contains(extensions)) {
                String script = FileUtils.readFileToString(file, "utf-8");
                Engine e = new Engine(script, req, resp, this);
                if (filter != null) e.setQueryUrl(filter);
                long end = System.currentTimeMillis();
                e.execute();
                Logger.info("[" + getName() + "][" + req.getMethod() + "][" + resp.getStatus() + "]" +
                        req.getRemoteAddr() +
                        " - " + req.getRequestURI() + "(" + (end - start) + "ms)");

            } else {
                FileInputStream inputStream = new FileInputStream(file);
                IOUtils.write(inputStream.readAllBytes(), resp.getOutputStream());
                inputStream.close();
            }
        } catch (Http403Exception e) {
            resp.setStatus(403);
            resp.getWriter().println(e.getPage());
        } catch (Http404Exception e) {
            resp.setStatus(404);
            resp.getWriter().println(e.getPage());
        } catch (HttpException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            Logger.logException(e);
            resp.setStatus(500);
            resp.getWriter().println(new Http500Exception(this).getPage());
        }
    }

    public boolean getPermission(String key) {
        if (config.permission == null) return false;
        if (!config.permission.containsKey(key)) return false;
        return config.permission.get(key);
    }

    private File getFile(String url) throws HttpException {
        File file = new File(config.path + url);
        if (!file.exists()) throw new Http404Exception(this);
        if (file.exists() && file.isDirectory()) {
            File page = new File(file + "/index.hs");
            if (!page.exists()) page = new File(file + "/index.hsp");
            if (!page.exists()) page = new File(file + "/index.html");
            if (!page.exists()) throw new Http403Exception(this);
            return page;
        }
        return file;
    }

    private String getExtensions(String filename) {
        //TODO 此方法待完善
        if (!filename.contains(".")) return null;
        String[] split = filename.split("\\.");
        return split[split.length - 1];
    }

    private String filter(String url) {
        for (Map.Entry<String, PseudoStatic> entry : pseudoStaticMap.entrySet()) {
            String target = entry.getValue().handler(url);
            if (target != null) {
                return target;
            }
        }
        return null;
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
                    pool.put(entry.getKey(), cpds);
                } catch (PropertyVetoException e) {
                    Logger.logException(e);
                }
            }
        }
    }

    @Override
    public void disable() {
        super.disable();
        for (Map.Entry<String, ComboPooledDataSource> entry : pool.entrySet()) {
            entry.getValue().close();
        }
    }
}
