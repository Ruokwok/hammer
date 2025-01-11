package cc.ruok.hammer;

import cc.ruok.hammer.site.WebSite;
import cn.hutool.core.io.watch.SimpleWatcher;
import cn.hutool.core.io.watch.WatchMonitor;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

public class ConfigWatchdog {

    private File file;
    private WatchMonitor dog;

    public ConfigWatchdog(File file) {
        this.file = file;
        dog = WatchMonitor.createAll(file, new SimpleWatcher() {
            @Override
            public void onModify(WatchEvent<?> event, Path currentPath) {
                Path path = (Path) event.context();
                if (!path.toFile().getName().endsWith(".yml")) return;
                String type = event.kind().name();
                try {
                    File _file = new File(file + "/" + path);
//                    if (type.equals("ENTRY_MODIFY")) WebServer.load(_file);
//                    if (type.equals("ENTRY_CREATE")) WebServer.load(_file);
//                    if (type.equals("ENTRY_DELETE") || type.equals("OVERFLOW")) {
//                        WebSite site = WebServer.getInstance().getWebSiteByConfig(_file.getName());
//                        site.disable();
//                    }
                    WebSite site = WebServer.getInstance().getWebSiteByConfig(_file.getName());
                    if (site == null) {
                        WebServer.load(_file);
                    } else {
                        site.disable();
                        site.enable();
                    }
                } catch (Exception e) {
                    Logger.logException(e);
                }
            }
        });
    }

    public void start() {
        dog.start();
    }

}
