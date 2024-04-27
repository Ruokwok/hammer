package cc.ruok.hammer;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class HammerConfig {

    public int httpPort;
    public int httpsPort;
    public ArrayList<String> scriptFileTypes = new ArrayList<>();
    public HashMap<String, String> fileHeader = new HashMap<>();

    public static HammerConfig load() throws IOException {
        File file = new File("server.yml");
        if (!file.exists()) {
            HammerConfig config = new HammerConfig();
            config.httpPort = 80;
            config.httpsPort = 443;
            config.scriptFileTypes.add("hsp");
            config.scriptFileTypes.add("hs");
            config.fileHeader.put("apk", "application/vnd.android.package-archive");
            config.fileHeader.put("css", "text/css");
            config.fileHeader.put("gif", "image/gif");
            config.fileHeader.put("htm", "text/html");
            config.fileHeader.put("html", "text/html");
            config.fileHeader.put("htx", "text/html");
            config.fileHeader.put("hso", "text/html");
            config.fileHeader.put("ico", "image/x-icon");
            config.fileHeader.put("img", "application/x-img");
            config.fileHeader.put("ipa", "application/vnd.iphone");
            config.fileHeader.put("jpeg", "image/jpeg");
            config.fileHeader.put("jpg", "image/jpeg");
            config.fileHeader.put("js", "application/x-javascript");
            config.fileHeader.put("pdf", "application/pdf");
            config.fileHeader.put("png", "image/png");
            config.fileHeader.put("ppt", "application/vnd.ms-powerpoint");
            config.fileHeader.put("txt", "text/plain");
            config.fileHeader.put("doc", "application/msword");
            config.fileHeader.put("docx", "application/msword");
            config.fileHeader.put("xhtml", "text/html");
            config.fileHeader.put("xls", "application/vnd.ms-excel");
            config.fileHeader.put("xlsx", "application/vnd.ms-excel");
            config.fileHeader.put("xml", "text/xml");
            YamlWriter writer = new YamlWriter(new FileWriter(file));
            writer.write(config);
            writer.close();
            return config;
        }
        YamlReader reader = new YamlReader(new FileReader(file));
        return reader.read(HammerConfig.class);
    }

}
