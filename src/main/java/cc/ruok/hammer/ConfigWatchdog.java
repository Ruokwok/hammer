package cc.ruok.hammer;

import cn.hutool.core.io.watch.SimpleWatcher;
import cn.hutool.core.io.watch.WatchMonitor;
import cn.hutool.core.io.watch.watchers.DelayWatcher;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

public class ConfigWatchdog {

    private File file;
    private WatchMonitor dog;

    public ConfigWatchdog(File file) {
        this.file = file;
        dog = WatchMonitor.createAll(file, new DelayWatcher(new SimpleWatcher() {
            @Override
            public void onModify(WatchEvent<?> event, Path currentPath) {
                Path path = (Path) event.context();
                if (!path.toFile().getName().endsWith(".yml")) return;
                try {
                    File _file = new File(file + "/" + path);
                    WebServer.load(_file);
                } catch (Exception e) {
                    Logger.logException(e);
                }
            }
        }, 500)
        );
    }

    public void start() {
        dog.start();
        Logger.info("Start config watchdog");
    }

    public void stop() {
        dog.close();
        Logger.info("Stop config watchdog");
    }

    public boolean isRunning() {
        return dog.isAlive();
    }

}
