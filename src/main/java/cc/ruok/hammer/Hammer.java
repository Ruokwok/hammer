package cc.ruok.hammer;

import cc.ruok.hammer.engine.Engine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class Hammer {

    public static final File CONFIG_PATH = new File("config");
    public static HammerConfig config;

    public static void main(String[] args) {
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
        try {
            config = HammerConfig.load();
            init(args);
            Engine.loadBaseJs();
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
        }
        if (!CONFIG_PATH.exists()) {
            CONFIG_PATH.mkdir();
            InputStream yml = Hammer.class.getResourceAsStream("/default.yml");
            Files.copy(yml, new File(CONFIG_PATH + "/default.yml").toPath());
        }
        File ssl = new File("ssl");
        if (!ssl.exists()) {
            ssl.mkdir();
        }
    }

}

