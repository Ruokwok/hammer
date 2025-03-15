package cc.ruok.hammer;

import cc.ruok.hammer.engine.Engine;
import cc.ruok.hammer.engine.api.EngineAPI;
import cc.ruok.hammer.plugin.HammerPlugin;
import cc.ruok.hammer.plugin.PluginManager;
import cn.hutool.core.util.XmlUtil;
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

public class Hammer {

    public static final File CONFIG_PATH = new File("config");
    public static final String PROCESS_PATH = "runtime";
    public static HammerConfig config;
    public static final long START_TIME = System.currentTimeMillis();
    public static Map<String, String> build;
    private static String token;
    private static String version = "Self-Build Version";
    private static ConfigWatchdog configWatchdog;

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
            EngineAPI.registerDefault();
            PluginManager.loadAll();
            WebServer.loadAll();
            WebServer server = WebServer.getInstance();
            startConfigWatchdog();
            server.start();
            Logger.info("Hammer is stopped.");
        } catch (Exception e) {
            Logger.logException(e);
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
                token = param.substring(param.indexOf("=") + 1);
            }
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
        Logger.info("Stopping server...");
        try {
            WebServer.unloadAll();
            new File(PROCESS_PATH).delete();
            WebServer.getInstance().stop();
            for (HammerPlugin plugin : new ArrayList<>(PluginManager.list)) {
                PluginManager.unload(plugin);
            }
        } catch (Exception e) {
            Logger.warning("An error occurred while stop server, force exit.");
            System.exit(1);
        }
    }

    public static void startConfigWatchdog() {
        if (configWatchdog != null && configWatchdog.isRunning()) return;
        configWatchdog = new ConfigWatchdog(CONFIG_PATH);
        configWatchdog.start();
    }

    public static void stopConfigWatchdog() {
        configWatchdog.stop();
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

}

