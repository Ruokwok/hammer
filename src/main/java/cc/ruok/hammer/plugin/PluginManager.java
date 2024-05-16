package cc.ruok.hammer.plugin;

import cc.ruok.hammer.Logger;
import com.esotericsoftware.yamlbeans.YamlReader;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PluginManager {

    public static void loadPlugin(File file) {
        try (ZipFile zipFile = new ZipFile(file)) {
            ZipEntry zipEntry = zipFile.getEntry("plugin.yml");
            InputStream inputStream = zipFile.getInputStream(zipEntry);
            YamlReader yaml = new YamlReader(IOUtils.toString(inputStream, StandardCharsets.UTF_8));
            PluginDescription description = yaml.read(PluginDescription.class);
            Logger.info("load plugin: " + description.name + "_v" + description.version);
            URL[] urls = { file.toURI().toURL() };
            URLClassLoader classLoader = new URLClassLoader(urls);
            Class<?> aClass = classLoader.loadClass(description.main);
            if (aClass.getSuperclass() != HammerPlugin.class) {
                throw new RuntimeException("the plugin class is not extended <HammerPlugin> : " + description.name);
            } else {
                Object obj = aClass.getDeclaredConstructor().newInstance();
                HammerPlugin plugin = (HammerPlugin) obj;
                plugin.onEnable();
            }
        } catch (Exception e) {
            Logger.logException(e);
        }
    }

    public static void loadAll() {
        File file = new File("plugins");
        File[] files = file.listFiles();
        if (files == null || files.length == 0) return;
        for (File jar : file.listFiles()) {
            if (jar.getName().endsWith(".jar")) {
                loadPlugin(jar);
            }
        }
    }

}
