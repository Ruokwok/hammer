package cc.ruok.hammer;

import cc.ruok.hammer.site.StaticWebSite;
import cc.ruok.hammer.site.WebSite;
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
    private HashMap<String, WebSite> sites = new HashMap<>();

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
        YamlReader reader = new YamlReader(new FileReader(yml));
        Config config = reader.read(Config.class);
        for (String domain : config.domain) {
            WebSite site = null;
            if (config.type.equalsIgnoreCase("static")) {
                site = new StaticWebSite(config);
            }
            getInstance().sites.putIfAbsent(domain, site);
        }
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

    public WebSite getWebSite(String domain) {
        return sites.get(domain);
    }
}
