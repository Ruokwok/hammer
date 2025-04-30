package cc.ruok.hammer.engine;

import cc.ruok.hammer.Logger;
import cc.ruok.hammer.engine.api.EngineException;
import cc.ruok.hammer.engine.api.EngineSystem;
import cc.ruok.hammer.site.ScriptWebSite;
import cn.hutool.core.io.FileUtil;
import cn.hutool.cron.task.Task;

import java.io.File;

public class CronTask implements Task {

    private String url;
    private ScriptWebSite site;
    private File file;
    private String path;

    public CronTask(String url, ScriptWebSite site) throws EngineException {
        this.url = url;
        this.site = site;
        if (url.contains("?")) {
            path = url.substring(0, url.indexOf("?"));
        } else {
            path = url;
        }
        this.file = new File(site.getPath() + "/" + path);
        if (!file.exists() || file.isDirectory()) {
            throw new EngineException("The file does not exist:" + path);
        }
    }

    @Override
    public void execute() {
        try {
            String str = FileUtil.readString(file, "utf-8");
            Engine task = new Engine(str, url, new Engine.NullWriter(), site, null);
            task.setRT(new EngineSystem(task));
            long start = System.currentTimeMillis();
            task.execute();
            Logger.info("[" + site.getName() + "][CRON][" + task.getStatus() + "] - " + path + "(" + (System.currentTimeMillis() - start) + "ms)");
        } catch (Exception e) {
            Logger.logException(e);
        }
    }
}
