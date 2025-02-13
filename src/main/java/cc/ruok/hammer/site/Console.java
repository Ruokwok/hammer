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
