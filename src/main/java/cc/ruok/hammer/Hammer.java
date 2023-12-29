package cc.ruok.hammer;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class Hammer {

    public static final File CONFIG_PATH = new File("config");

    public static void main(String[] args) {
        try {
            init();
            WebServer.loadAll();
            WebServer server = WebServer.getInstance();
            server.start();
        } catch (Exception e) {
            Logger.logException(e);
        }
    }

    public static void init() throws IOException {
        if (!CONFIG_PATH.exists()) {
            CONFIG_PATH.mkdir();
            InputStream yml = Hammer.class.getResourceAsStream("/default.yml");
            Files.copy(yml, new File(CONFIG_PATH + "/default.yml").toPath());
        }
    }

}

