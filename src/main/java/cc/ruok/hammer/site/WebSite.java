package cc.ruok.hammer.site;

import cc.ruok.hammer.*;
import cc.ruok.hammer.error.Http403Exception;
import cc.ruok.hammer.error.Http404Exception;
import cc.ruok.hammer.error.Http500Exception;
import cc.ruok.hammer.error.HttpException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public abstract class WebSite {

    Config config;
    File path;
    public HashMap<String, PseudoStatic> pseudoStaticMap = new HashMap<>();

    public WebSite(Config config) {
        if (config == null) return;
        this.config = config;
        path = new File(config.path);
        if (!path.exists()) {
            try {
                FileUtils.forceMkdir(path);
            } catch (IOException e) {
                Logger.logException(e);
            }
        }
    }

    public static String notSite(String domain) {
        try {
            String page = IOUtils.toString(WebSite.class.getResourceAsStream("/default_page/no_site.html"), "utf8");
            return page.replaceAll("\\{\\{domain}}", domain);
        } catch (IOException e) {
            Logger.logException(e);
            return null;
        }
    }

    public String getName() {
        return config.name;
    }

    public File getPath() {
        return path;
    }

    public String[] getDomains() {
        return config.domain.toArray(new String[0]);
    }

    public String getErrorPage(int code) {
        try {
            String c = "error_" + code;
            if (config.error_page != null && config.error_page.containsKey(c))
                return FileUtils.readFileToString(new File(path + "/" + config.error_page.get(c)), "utf8");
        } catch (Exception e) {
            Logger.logException(e);
        }
        return null;
    }

    public void enable() {
        for (String domain : config.domain) {
            WebServer.getInstance().putDomain(domain, this);
        }
        if (config.pseudo_static != null && config.pseudo_static.size() > 0) {
            for (String exp : config.pseudo_static) {
                PseudoStatic pseudoStatic = new PseudoStatic(exp);
                if (pseudoStatic.isValid()) pseudoStaticMap.put(pseudoStatic.getOrigin(), pseudoStatic);
            }
        }
        Logger.info("Enabled website: " + config.name + "(" + config.type + ").");
    }

    public void disable() {
        pseudoStaticMap.clear();
        WebServer.unload(config.getFile());
    }

    public void handler(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            resp.setCharacterEncoding("utf8");
            String filter = filter(req.getServletPath());
            String filePath;
            if (filter == null) {
                filePath = req.getServletPath();
                if (checkProtect(filePath)) {
                    throw new Http403Exception(this);
                }
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
            execute(file, req, resp);
        } catch (HttpException e) {
            try {
                resp.setStatus(e.getCode());
                String page = config.error_page.get("error_" + e.getCode());
                File file;
                if (page == null) page = "~/error_" + e.getCode() + ".html";
                if (page.startsWith("~")) {
                    String string = IOUtils.toString(getClass().getResourceAsStream(page.replace("~", "/default_page")), "utf8");
                    resp.getWriter().println(string.replaceAll("\\$\\{version}", Hammer.getVersion()));
                } else {
                    file = getFile(page);
                    execute(file, req, resp);
                }
            } catch (HttpException ex) {
                Logger.logException(e);
                resp.setStatus(500);
                resp.getWriter().println(new Http500Exception(this).getPage().replaceAll("\\$\\{version}", Hammer.getVersion()));
            }
        } catch (Exception e) {
            Logger.logException(e);
            resp.setStatus(500);
            resp.getWriter().println(new Http500Exception(this).getPage().replaceAll("\\$\\{version}", Hammer.getVersion()));
        }
    }

    protected File getFile(String url) throws HttpException {
        File file = new File(config.path + url);
        if (!file.exists()) throw new HttpException(this, 404);
        if (file.exists() && file.isDirectory()) {
            File page = new File(file + "/index.hs");
            if (!page.exists()) page = new File(file + "/index.hsp");
            if (!page.exists()) page = new File(file + "/index.html");
            if (!page.exists()) throw new HttpException(this, 403);
            return page;
        }
        return file;
    }

    protected String filter(String url) {
        for (Map.Entry<String, PseudoStatic> entry : pseudoStaticMap.entrySet()) {
            String target = entry.getValue().handler(url);
            if (target != null) {
                return target;
            }
        }
        return null;
    }

    protected boolean checkProtect(String path) {
        if (config.protects == null) return false;
        for (String exp : config.protects) {
            //TODO 这里只是简单判断表达式是否是目标字符串的前段，不靠谱，以后有时间再改。
            if (exp.equals(path)) return true;
            if (path.startsWith(exp)) return true;
        }
        return false;
    }

    public static void input(File file, OutputStream os) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        os.flush();
        inputStream.close();
    }

    public abstract void execute(File file, HttpServletRequest req, HttpServletResponse resp) throws IOException;
}
