package cc.ruok.hammer;

import cc.ruok.hammer.engine.ContextPool;
import cc.ruok.hammer.engine.Engine;
import cc.ruok.hammer.engine.api.EngineAPI;
import cc.ruok.hammer.plugin.HammerPlugin;
import cc.ruok.hammer.plugin.PluginManager;
import cc.ruok.hammer.site.ScriptWebSite;
import cc.ruok.hammer.site.WebSite;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.setting.yaml.YamlUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.graalvm.polyglot.Context;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Hammer {

    public static final File CONFIG_PATH = new File("config");
    public static final String PROCESS_PATH = "runtime";
    public static HammerConfig config;
    public static final long START_TIME = System.currentTimeMillis();
    public static Map<String, Object> build;
    private static String token;
    private static String version = "self-build version";
    private static ConfigWatchdog configWatchdog;
    private static final AtomicBoolean STOPPING = new AtomicBoolean(false);

    static {
        try {
            build = YamlUtil.load(Hammer.class.getResourceAsStream("/META-INF/MANIFEST.MF"), Map.class);
        } catch (Exception e) {
            build = null;
        }
    }

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("--version")) {
            System.out.println("Hammer version - " + build.get("Version"));
            System.out.println("Build of JDK" + build.get("Build-Jdk-Spec") + "\t" + build.get("Build-Date"));
            System.exit(0);
        } else if (args.length == 1 && args[0].equals("--version=simple")) {
            System.out.print(build.get("Version"));
            System.exit(0);
        }
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
        try {
            Logger.info("The hammer is starting...");
            testEngine();
            config = HammerConfig.load();
            init(args);
            Engine.loadBaseJs();
            ContextPool.init(config.contextPoolSize);
            EngineAPI.registerDefault();
            PluginManager.loadAll();
            WebServer.loadAll();
            WebServer server = WebServer.getInstance();
            startConfigWatchdog();
            startCronScheduler();
            server.start();
            Logger.info("Hammer is stopped.");
        } catch (Exception e) {
            Logger.logException(e);
            System.exit(1);
        }
    }

    public static void init(String[] args) throws IOException {
        try {
            String string = IOUtils.resourceToString("/META-INF/maven/cc.ruok.hammer/hammer/pom.xml", Charset.defaultCharset());
            Map<String, Object> xml = XmlUtil.xmlToMap(string);
            Object v = xml.get("version");
            if (v instanceof String) {
                version = (String) v;
            }
        } catch (Exception e) {
            Logger.warning("Detected using a self-build version.");
        }
        Logger.info("Version: " + version);
        for (String param : args) {
            if (param.startsWith("--httpPort=")) config.httpPort = Integer.parseInt(param.substring(param.indexOf("=") + 1));
            if (param.startsWith("--httpsPort=")) config.httpsPort = Integer.parseInt(param.substring(param.indexOf("=") + 1));
            if (param.equals("--installed=true") || param.equals("-install")) {
                File process = new File(PROCESS_PATH);
                if (!process.exists()) {
                    process.mkdir();
                }
                long pid = ManagementFactory.getRuntimeMXBean().getPid();
                FileUtils.writeStringToFile(new File(PROCESS_PATH + "/PID"), String.valueOf(pid));
                FileUtils.writeStringToFile(new File(PROCESS_PATH + "/HTTP"), String.valueOf(config.httpPort));
                FileUtils.writeStringToFile(new File(PROCESS_PATH + "/HTTPS"), String.valueOf(config.httpsPort));
            }
            if (param.startsWith("--token=")) {
                String value = param.substring(param.indexOf("=") + 1);
                if (!value.isEmpty()) token = value;
            }
        }
        if (token == null) {
            String env = System.getenv("HAMMER_TOKEN");
            if (env != null && !env.isEmpty()) token = env;
        }
        if (!CONFIG_PATH.exists()) {
            CONFIG_PATH.mkdir();
            InputStream yml = Hammer.class.getResourceAsStream("/default.yml");
            Files.copy(yml, new File(CONFIG_PATH + "/default.yml").toPath());
        }
        File ssl = new File("ssl");
        if (!ssl.exists()) ssl.mkdir();
        File plugins = new File("plugins");
        if (!plugins.exists()) plugins.mkdir();
        File temp = new File("temp");
        FileUtils.deleteDirectory(temp);
    }

    public static String getToken() {
        return token;
    }

    public static String getVersion() {
        return version;
    }

    public static void stop() {
        if (!STOPPING.compareAndSet(false, true)) return;
        Logger.info("Stopping server...");
        Thread stopper = new Thread(() -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException ignored) {
            }
            stopServices();
            System.exit(0);
        }, "hammer-stop");
        stopper.start();
        Thread halt = new Thread(() -> {
            try {
                stopper.join(30000);
            } catch (InterruptedException ignored) {
            }
            System.exit(0);
        }, "hammer-halt");
        halt.setDaemon(true);
        halt.start();
    }

    private static void stopServices() {
        try {
            CronUtil.stop();
        } catch (Exception e) {
            Logger.logException(e);
        }
        try {
            WebServer.getInstance().stop();
        } catch (Exception e) {
            Logger.logException(e);
        }
        try {
            WebServer.unloadAll();
        } catch (Exception e) {
            Logger.logException(e);
        }
        try {
            ContextPool.close();
        } catch (Exception e) {
            Logger.logException(e);
        }
        try {
            FileUtils.deleteDirectory(new File(PROCESS_PATH));
        } catch (Exception e) {
            Logger.logException(e);
        }
        for (HammerPlugin plugin : new ArrayList<>(PluginManager.list)) {
            try {
                PluginManager.unload(plugin);
            } catch (Exception e) {
                Logger.logException(e);
            }
        }
    }

    public static void startConfigWatchdog() {
        if (configWatchdog != null && configWatchdog.isRunning()) return;
        configWatchdog = new ConfigWatchdog(CONFIG_PATH);
        configWatchdog.start();
    }

    public static void stopConfigWatchdog() {
        if (configWatchdog != null) {
            configWatchdog.stop();
            configWatchdog = null;
        }
    }
    public static void testEngine() {
        Logger.debug("Testing JavaScript engine.");
        try {
            Context engine = Context.newBuilder("js").allowAllAccess(true).build();
            engine.eval("js", "");
        } catch (IllegalArgumentException e) {
//            Logger.logException(e);
            Logger.error("The js module is not installed, please install it and try again.");
            Logger.error("If you using GraalVM, please try execute command: gu install js");
            System.exit(1);
        }
    }

    public static void startCronScheduler() {
        WebServer server = WebServer.getInstance();
        Map<String, WebSite> sites = server.getSites();
        boolean cron = false;
        for (WebSite site : sites.values()) {
            if (site instanceof ScriptWebSite s) {
                if (s.hasCron()) {
                    cron = true;
                    break;
                }
            }
        }
        if (cron) {
            CronUtil.setMatchSecond(true);
            CronUtil.start();
            Logger.info("Start CRON scheduler.");
        }
    }

}

