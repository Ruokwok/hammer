package cc.ruok.hammer;

import cc.ruok.hammer.site.ScriptWebSite;
import cc.ruok.hammer.site.StaticWebSite;
import cc.ruok.hammer.site.WebSite;
import cn.hutool.core.io.FileUtil;
import com.esotericsoftware.yamlbeans.YamlReader;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebServer {

    private static WebServer that = new WebServer();
    private Server server;
    private final HashMap<String, WebSite> sites = new HashMap<>();
    private final HashMap<String, WebSite> fileSiteMap = new HashMap<>();
    private final HashMap<String, String> ssl = new HashMap<>();

    private ServerConnector connector;

    private WebServer() {
    }

    public static WebServer getInstance() {
        return that;
    }

    public void start() throws Exception {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.addServlet(WebServlet.class, "/");
        server = new Server(80);
        server.setHandler(context);
        connector = new ServerConnector(server);
        if (ssl.size() > 0) {
            for (Map.Entry<String, String> entry : ssl.entrySet()) {
                addSSL(entry.getKey(), entry.getValue());
            }
        }
        connector.setPort(443);
        server.addConnector(connector);
        server.start();
//        server.join();
    }

    public void loadSSL(String keyFile, String password) {
        try {
            SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
            sslContextFactory.setKeyStorePath("ssl/" + keyFile);
            sslContextFactory.setKeyStorePassword(password);
            sslContextFactory.setKeyManagerPassword(password);
            connector.addConnectionFactory(new SslConnectionFactory(sslContextFactory, "http/1.1"));
        } catch (Exception e) {
            Logger.logException(e);
        }
    }

    public void addSSL(String keyFile, String password) {
        ssl.put(keyFile, password);
    }

    public static void load(File yml) throws IOException {
        unload(yml);
        YamlReader reader = new YamlReader(new FileReader(yml));
        Config config = reader.read(Config.class);
        reader.close();
        WebSite site = null;
        if (config.ssl_keystore != null) {
            WebServer.getInstance().addSSL(config.ssl_keystore, config.ssl_password);
        }
        if (config.type.equalsIgnoreCase("static")) {
            site = new StaticWebSite(config);
        } else if (config.type.equalsIgnoreCase("script")) {
            site = new ScriptWebSite(config);
        }
        getInstance().fileSiteMap.put(FileUtil.getAbsolutePath(yml), site);
        for (String domain : config.domain) {
            getInstance().sites.putIfAbsent(domain, site);
        }
        Logger.info("start website: " + config.name + "(" + config.type + ").");
    }

    public static void loadAll() {
        for (File file : Hammer.CONFIG_PATH.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".yml")) {
                try {
                    load(file);
                } catch (IOException e) {
                    Logger.logException(e);
                }
            }
        }
    }

    public static void unload(File yml) {
            String path = FileUtil.getAbsolutePath(yml);
        WebSite site = getInstance().fileSiteMap.get(path);
        if (site != null) {
            getInstance().fileSiteMap.remove(path);
            for (String domain : site.getDomains()) {
                getInstance().sites.remove(domain);
            }
            Logger.info("stop site: " + site.getName());
        }
    }

    public WebSite getWebSite(String domain) {
        return sites.get(domain);
    }
}
