package cc.ruok.hammer;

import cc.ruok.hammer.site.ScriptWebSite;
import cc.ruok.hammer.site.StaticWebSite;
import cc.ruok.hammer.site.WebSite;
import cn.hutool.core.io.FileUtil;
import com.esotericsoftware.yamlbeans.YamlReader;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.util.*;

public class WebServer {

    private static WebServer that = new WebServer();
    private Server server;
    private final HashMap<String, WebSite> sites = new HashMap<>();
    private final HashMap<String, WebSite> fileSiteMap = new HashMap<>();
    private final HashMap<String, SslKey> sniMap = new HashMap<>();

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
        SslContextFactory.Server factory = new SslContextFactory.Server();
        connector = new ServerConnector(server, factory);
        SslKey sslKey = margeSSL();
        if (sslKey != null) {
            SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
            sslContextFactory.setKeyStorePath(sslKey.isMerged ? sslKey.getFileName() : "ssl/" + sslKey.getFileName());
            sslContextFactory.setKeyStorePassword(sslKey.getPassword());
            sslContextFactory.setKeyManagerPassword(sslKey.getPassword());
            connector.addConnectionFactory(new SslConnectionFactory(sslContextFactory, "http/1.1"));
        }
        connector.setPort(443);
        server.addConnector(connector);
        server.start();
    }

    public void loadSSL(String keyFile, String password) {
        try {
            SslKey sslKey = new SslKey(keyFile, password);
            sniMap.put(keyFile, sslKey);
        } catch (Exception e) {
            Logger.logException(e);
        }
    }

    private SslKey margeSSL() throws Exception {
        if (sniMap.size() == 0) return null;
        if (sniMap.size() == 1) {
            for (Map.Entry<String, SslKey> entry : sniMap.entrySet()) {
                return entry.getValue();
            }
        } else {
            KeyStore ksMerged = KeyStore.getInstance("JKS");
            ksMerged.load(null, null);
            List<KeyStore> list = new ArrayList<>();
            for (Map.Entry<String, SslKey> entry : sniMap.entrySet()) {
                KeyStore ks = KeyStore.getInstance("JKS");
                FileInputStream fis = new FileInputStream("ssl/" + entry.getValue().getFileName());
                ks.load(fis, entry.getValue().getPassword().toCharArray());
                fis.close();

                Enumeration<String> aliases1 = ks.aliases();
                while (aliases1.hasMoreElements()) {
                    String alias = aliases1.nextElement();
                    if (ks.isKeyEntry(alias)) {
                        Key key = ks.getKey(alias, entry.getValue().getPassword().toCharArray());
                        java.security.cert.Certificate[] chain = ks.getCertificateChain(alias);
                        ksMerged.setKeyEntry(alias, key, "223344".toCharArray(), chain);
                    } else {
                        Certificate cert = ks.getCertificate(alias);
                        ksMerged.setCertificateEntry(alias, cert);
                    }
                }
            }
            FileOutputStream fos = new FileOutputStream("merged.jks");
            ksMerged.store(fos, "223344".toCharArray());
            fos.close();
            SslKey sslKey = new SslKey("merged.jks", "223344");
            sslKey.isMerged = true;
            return sslKey;
        }
        return null;
    }

    public void addSSL(String keyFile, String password) {
        sniMap.put(keyFile, new SslKey(keyFile, password));
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

    public class SslKey {
        private boolean isMerged = false;
        private String file;
        private String password;

        public SslKey(String file, String password) {
            this.file = file;
            this.password = password;
        }

        protected String getFileName() {
            return file;
        }

        protected String getPassword() {
            return password;
        }

        protected boolean isMerged() {
            return isMerged;
        }
    }
}
