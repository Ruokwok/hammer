package cc.ruok.hammer;

import cc.ruok.hammer.engine.Engine;
import cc.ruok.hammer.engine.api.EngineAPI;
import cc.ruok.hammer.plugin.PluginManager;
import cn.hutool.core.util.XmlUtil;
import com.esotericsoftware.yamlbeans.YamlReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Map;

public class Hammer {

    public static final File CONFIG_PATH = new File("config");
    public static final String PROCESS_PATH = "runtime";
    public static HammerConfig config;
    public static final long START_TIME = System.currentTimeMillis();
    public static Map<String, String> build;
    private static String token;
    private static String version = "Self-Build Version";

    static {
        try {
            String string = IOUtils.resourceToString("/META-INF/MANIFEST.MF", Charset.defaultCharset());
            YamlReader reader = new YamlReader(string);
            build = reader.read(Map.class);
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
            Logger.info("the hammer is starting...");
            config = HammerConfig.load();
            init(args);
            Engine.loadBaseJs();
            EngineAPI.registerDefault();
            PluginManager.loadAll();
            WebServer.loadAll();
            WebServer server = WebServer.getInstance();
            server.start();
            new ConfigWatchdog(new File("config")).start();
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
        Logger.info("version: " + version);
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

}

