package cc.ruok.hammer;

import cc.ruok.hammer.site.ScriptWebSite;
import cc.ruok.hammer.site.StaticWebSite;
import cc.ruok.hammer.site.WebSite;
import cn.hutool.core.io.FileUtil;
import com.esotericsoftware.yamlbeans.YamlReader;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class WebServer {

    private static WebServer that = new WebServer();
    private Server server;
    private final HashMap<String, WebSite> sites = new HashMap<>();
    private final HashMap<String, WebSite> fileSiteMap = new HashMap<>();

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

//        if (serverConfig.ssl) {
//            HttpConfiguration config = new HttpConfiguration();
//            config.setSecureScheme("https");
//            config.setSecurePort(443);
//            SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
//            sslContextFactory.setKeyStorePath("ssl/keystore.jks");
//            sslContextFactory.setKeyStorePassword(serverConfig.keystore_password);
//            sslContextFactory.setKeyManagerPassword(serverConfig.keystore_password);
//            ServerConnector httpsConnector = new ServerConnector(server,
//                    new SslConnectionFactory(sslContextFactory, "http/1.1"),
//                    new HttpConnectionFactory(config));
//            httpsConnector.setPort(443);
//            server.addConnector(httpsConnector);
//        }

        server.start();
//        server.join();
    }

    public static void load(File yml) throws IOException {
        unload(yml);
        YamlReader reader = new YamlReader(new FileReader(yml));
        Config config = reader.read(Config.class);
        reader.close();
        WebSite site = null;
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
