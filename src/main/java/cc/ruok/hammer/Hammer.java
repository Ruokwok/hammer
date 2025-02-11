package cc.ruok.hammer;

import cc.ruok.hammer.engine.Engine;
import cc.ruok.hammer.engine.api.EngineAPI;
import cc.ruok.hammer.plugin.PluginManager;
import cn.hutool.core.io.FileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;

public class Hammer {

    public static final File CONFIG_PATH = new File("config");
    public static final File PROCESS_PATH = new File("process");
    public static HammerConfig config;
    public static final String VERSION = "1.0-SNAPSHOT";

    public static void main(String[] args) {
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
        try {
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
        for (String param : args) {
            if (param.startsWith("--httpPort=")) config.httpPort = Integer.parseInt(param.substring(param.indexOf("=") + 1));
            if (param.startsWith("--httpsPort=")) config.httpsPort = Integer.parseInt(param.substring(param.indexOf("=") + 1));
            if (param.equals("--installed=true") || param.equals("-install")) {
                if (!PROCESS_PATH.exists()) {
                    PROCESS_PATH.mkdir();
                }
                long pid = ManagementFactory.getRuntimeMXBean().getPid();
                FileUtil.writeUtf8String(String.valueOf(pid), PROCESS_PATH + "/PID");
                FileUtil.writeUtf8String(String.valueOf(config.httpPort), PROCESS_PATH + "/HTTP");
                FileUtil.writeUtf8String(String.valueOf(config.httpsPort), PROCESS_PATH + "/HTTPS");
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

}

