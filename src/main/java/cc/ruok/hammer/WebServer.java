package cc.ruok.hammer;

import cc.ruok.hammer.site.Console;
import cc.ruok.hammer.site.ScriptWebSite;
import cc.ruok.hammer.site.StaticWebSite;
import cc.ruok.hammer.site.WebSite;
import cn.hutool.core.io.FileUtil;
import cn.hutool.setting.yaml.YamlUtil;
import jakarta.servlet.MultipartConfigElement;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.*;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.util.*;

public class WebServer {

    private static WebServer that = new WebServer();
    private Server server;
    private final Hashtable<String, WebSite> sites = new Hashtable<>();
    private final HashMap<String, WebSite> fileSiteMap = new HashMap<>();
    private final HashMap<String, SslKey> sniMap = new HashMap<>();
    private final HashMap<String, WebSite> configMap = new HashMap<>();

    private ServerConnector connector;

    private WebServer() {
        sites.put("console..cmd", new Console());
    }

    public static WebServer getInstance() {
        return that;
    }

    public void start() throws Exception {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        String temp = Paths.get("temp").toFile().getAbsolutePath();
        MultipartConfigElement multipartConfigElement = new MultipartConfigElement(temp, 20971520 *10, 20971520 *10, 0);
        ServletHolder servletHolder = new ServletHolder(WebServlet.class);
        servletHolder.getRegistration().setMultipartConfig(multipartConfigElement);
        context.addServlet(servletHolder, "/");

        server = new Server(Hammer.config.httpPort);
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
        connector.setPort(Hammer.config.httpsPort);
        server.addConnector(connector);
        server.start();
        Logger.info("Hammer is started. (" + (System.currentTimeMillis() - Hammer.START_TIME) + "ms)");
        server.join();
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
            Logger.info("Merging multiple SSL keystore");
            return sslKey;
        }
        return null;
    }

    public void addSSL(String keyFile, String password) {
        sniMap.put(keyFile, new SslKey(keyFile, password));
    }

    public static void load(File yml) throws IOException {
        WebSite ws = getInstance().getWebSiteByConfig(yml.getName());
        if (ws != null) ws.disable();
        Config config = YamlUtil.load(new FileReader(yml), Config.class);
        config.setFile(yml);
        WebSite site = null;
        if (config.ssl_keystore != null) {
            WebServer.getInstance().addSSL(config.ssl_keystore, config.ssl_password);
        }
        if (config.type.equalsIgnoreCase("static")) {
            site = new StaticWebSite(config);
        } else if (config.type.equalsIgnoreCase("script")) {
            site = new ScriptWebSite(config);
        }
        getInstance().configMap.putIfAbsent(yml.getName(), site);
        getInstance().fileSiteMap.put(FileUtil.getAbsolutePath(yml), site);
        if (site != null) {
            site.enable();
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

    public static void unload(File yml) {
        String path = FileUtil.getAbsolutePath(yml);
        WebSite site = getInstance().fileSiteMap.get(path);
        if (site != null) {
            getInstance().fileSiteMap.remove(path);
            for (String domain : site.getDomains()) {
                getInstance().sites.remove(domain);
                getInstance().configMap.remove(yml.getName());
            }
            Logger.info("Disabled site: " + site.getName());
        }
    }

    public static void unloadAll() {
        Hammer.stopConfigWatchdog();
        Map<String, WebSite> sites = WebServer.getInstance().getSites();
        ArrayList<WebSite> list = new ArrayList<>();
        for (Map.Entry<String, WebSite> entry : sites.entrySet()) {
            list.add(entry.getValue());
        }
        for (WebSite site : list) {
            site.disable();
        }
    }

    public WebSite getWebSite(String domain) {
        return sites.get(domain);
    }

    public WebSite getWebSiteByConfig(String yml) {
        return configMap.get(yml);
    }

    public void putDomain(String domain, WebSite site) {
        sites.putIfAbsent(domain, site);
    }

    public List<WebSite> getWebSites() {
        ArrayList<WebSite> list = new ArrayList<>();
        for (Map.Entry<String, WebSite> entry: sites.entrySet()) {
            if (!list.contains(entry.getValue())) {
                list.add(entry.getValue());
            }
        }
        return list;
    }

    protected void stop() throws Exception {
        server.stop();
        connector.stop();
    }

    protected Map<String, WebSite> getSites() {
        return sites;
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
