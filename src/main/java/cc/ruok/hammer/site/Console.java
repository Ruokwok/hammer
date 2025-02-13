package cc.ruok.hammer.site;

import cc.ruok.hammer.Config;
import cc.ruok.hammer.engine.Engine;
import cc.ruok.hammer.plugin.HammerPlugin;
import cc.ruok.hammer.plugin.PluginManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class Console extends WebSite {

    public Console() {
        super(null);
    }

    @Override
    public void handler(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String host = req.getRemoteHost();
        if (!host.equals("[0:0:0:0:0:0:0:1]") && !host.equals("127.0.0.1")) return;
        Echo echo = new Echo();
        try {
            String url[] = req.getRequestURI().substring(1).split("/");
            if (url[0].equals("plugins")) {
                echo.println();
                echo.println("Total " + PluginManager.list.size() + " plugins");
                for (HammerPlugin plugin: PluginManager.list) {
                    echo.print("    " + plugin.getDescription().name);
                    echo.println(" - v" + plugin.getDescription().version);
                }
                echo.println();
                Map<String, Object> apiMap = Engine.getApiMap();
                echo.println("Total " + apiMap.size() + " modules");
                for (Map.Entry<String, Object> entry: apiMap.entrySet()) {
                    echo.print("    [" + entry.getKey() + "] type:");
                    String type = "Object";
                    if (entry.getValue() instanceof Class) {
                        type = "Class";
                    }
                    echo.println(type);
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

    static class Echo {

        public StringBuffer s = new StringBuffer();

        public void print(String str) {
            s.append(str);
        }

        public void println(String str) {
            s.append(str).append("\n");
        }

        public void println() {
            s.append("\n");
        }

        @Override
        public String toString() {
            return s.toString();
        }

    }
}
