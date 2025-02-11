package cc.ruok.hammer.site;

import cc.ruok.hammer.Config;
import cc.ruok.hammer.plugin.HammerPlugin;
import cc.ruok.hammer.plugin.PluginManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;

public class Console extends WebSite {

    public Console() {
        super(null);
    }

    @Override
    public void handler(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String echo = "error";
        try {
            String url[] = req.getRequestURI().substring(1).split("/");
            if (url[0].equals("plugins")) {
                echo = "\nTotal " + PluginManager.list.size() + " plugins\n";
                for (HammerPlugin plugin: PluginManager.list) {
                    echo += plugin.getDescription().name;
                    echo += " - v" + plugin.getDescription().version;
                    echo += "\n";
                }
            }
        } catch (Exception e) {
        } finally {
            resp.getWriter().print(echo);
        }
    }

    @Override
    public void execute(File file, HttpServletRequest req, HttpServletResponse resp) throws IOException {
    }
}
