package cc.ruok.hammer;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class HammerConfig {

    public int httpPort;
    public int httpsPort;
    public int contextPoolSize = Math.max(8, Runtime.getRuntime().availableProcessors() * 2);
    public ArrayList<String> scriptFileTypes = new ArrayList<>();
    public HashMap<String, String> fileHeader = new HashMap<>();

    public static DumperOptions options = new DumperOptions();

    static {
        options.setCanonical(false);
        options.setExplicitStart(false);
        options.setExplicitEnd(false);
    }

    public static HammerConfig load() throws IOException {
        File file = new File("server.yml");
        if (!file.exists()) {
            HammerConfig config = new HammerConfig();
            config.httpPort = 80;
            config.httpsPort = 443;
            config.scriptFileTypes = defaultScriptFileTypes();
            config.fileHeader = defaultFileHeader();
            Representer customRepresenter = new Representer(new DumperOptions());
            customRepresenter.addClassTag(HammerConfig.class, Tag.MAP);
            Yaml yaml = new Yaml(new Constructor(HammerConfig.class, new LoaderOptions()), customRepresenter);
            try (FileWriter writer = new FileWriter(file)) {
                yaml.dump(config, writer);
            }
            return config;
        } else {
            Yaml yaml = new Yaml(options);
            HammerConfig config;
            try (FileReader reader = new FileReader(file)) {
                config = yaml.loadAs(reader, HammerConfig.class);
            }
            if (config == null) config = new HammerConfig();
            if (config.httpPort <= 0) config.httpPort = 80;
            if (config.httpsPort <= 0) config.httpsPort = 443;
            if (config.scriptFileTypes == null || config.scriptFileTypes.isEmpty()) {
                config.scriptFileTypes = defaultScriptFileTypes();
            }
            if (config.fileHeader == null || config.fileHeader.isEmpty()) {
                config.fileHeader = defaultFileHeader();
            }
            if (config.contextPoolSize <= 0) {
                config.contextPoolSize = Math.max(8, Runtime.getRuntime().availableProcessors() * 2);
            }
            return config;
        }
    }

    public static ArrayList<String> defaultScriptFileTypes() {
        ArrayList<String> list = new ArrayList<>();
        list.add("hsp");
        list.add("hs");
        list.add("hspx");
        return list;
    }

    public static HashMap<String, String> defaultFileHeader() {
        HashMap<String, String> map = new HashMap<>();
        map.put("apk", "application/vnd.android.package-archive");
        map.put("css", "text/css");
        map.put("gif", "image/gif");
        map.put("htm", "text/html");
        map.put("html", "text/html");
        map.put("htx", "text/html");
        map.put("hso", "text/html");
        map.put("ico", "image/x-icon");
        map.put("img", "application/x-img");
        map.put("ipa", "application/vnd.iphone");
        map.put("jpeg", "image/jpeg");
        map.put("jpg", "image/jpeg");
        map.put("js", "application/x-javascript");
        map.put("pdf", "application/pdf");
        map.put("png", "image/png");
        map.put("ppt", "application/vnd.ms-powerpoint");
        map.put("txt", "text/plain");
        map.put("doc", "application/msword");
        map.put("docx", "application/msword");
        map.put("xhtml", "text/html");
        map.put("xls", "application/vnd.ms-excel");
        map.put("xlsx", "application/vnd.ms-excel");
        map.put("xml", "text/xml");
        return map;
    }

}
