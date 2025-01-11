package cc.ruok.hammer.site;

import cc.ruok.hammer.Config;
import cc.ruok.hammer.Logger;
import cc.ruok.hammer.WebServer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;

public abstract class WebSite {

    Config config;
    File path;

    public WebSite(Config config) {
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
            String c = String.valueOf(code);
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
        Logger.info("enabled website: " + config.name + "(" + config.type + ").");
    }

    public void disable() {
        WebServer.unload(config.getFile());
    }

    public abstract void handler(HttpServletRequest req, HttpServletResponse resp) throws IOException;
}
