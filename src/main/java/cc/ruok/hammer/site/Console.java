package cc.ruok.hammer.site;

import cc.ruok.hammer.Hammer;
import cc.ruok.hammer.engine.Engine;
import cc.ruok.hammer.plugin.HammerPlugin;
import cc.ruok.hammer.plugin.PluginManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.Map;

public class Console extends WebSite {

    public Console() {
        super(null);
    }

    @Override
    public void handler(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String host = req.getRemoteHost();
        if (!host.equals("[0:0:0:0:0:0:0:1]") && !host.equals("127.0.0.1")) return;
        if (Hammer.getToken() == null || !Hammer.getToken().equals(req.getHeader("Token"))) return;
        Echo echo = new Echo();
        try {
            String[] url = req.getRequestURI().substring(1).split("/");
            if (url[0].equals("plugins")) {
                echo = plugins();
            } else if (url[0].equals("status")) {
                echo = status();
            } else if (url[0].equals("stop")) {
                Hammer.stop();
            }
        } catch (Exception e) {
        } finally {
            resp.getWriter().print(echo);
        }
    }

    private Echo plugins() {
        Echo echo = new Echo();
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
        return echo;
    }

    private Echo status() {
        Echo echo = new Echo();
        long time = System.currentTimeMillis() - Hammer.START_TIME;
        echo.println();
        echo.println("Already run " + formatTime(time));
        echo.println("PID: " + ManagementFactory.getRuntimeMXBean().getPid());
        echo.println("Threads: " + ManagementFactory.getGarbageCollectorMXBeans().size());
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryBean.getHeapMemoryUsage();
        long heapInitial = heapMemoryUsage.getInit();
        long heapUsed = heapMemoryUsage.getUsed();
        long heapCommitted = heapMemoryUsage.getCommitted();
        long heapMax = heapMemoryUsage.getMax();
        echo.println("Heap Memory:");
        echo.println("    Initial: " + heapInitial / (1024 * 1024) + " MB");
        echo.println("    Used: " + heapUsed / (1024 * 1024) + " MB");
        echo.println("    Committed: " + heapCommitted / (1024 * 1024) + " MB");
        echo.println("    Max: " + heapMax / (1024 * 1024) + " MB");
        echo.println("JRE Information:");
        echo.println("    JVM: " + System.getProperty("java.vendor") + " " + System.getProperty("java.vm.name"));
        echo.println("    Version: " + System.getProperty("java.vm.version"));
        return echo;
    }

    public String formatTime(long time) {
        final long MS_PER_DAY = 24 * 60 * 60 * 1000L;
        final long MS_PER_HOUR = 60 * 60 * 1000L;
        final long MS_PER_MINUTE = 60 * 1000L;
        long days = time / MS_PER_DAY;
        time %= MS_PER_DAY;
        long hours = time / MS_PER_HOUR;
        time %= MS_PER_HOUR;
        long minutes = time / MS_PER_MINUTE;
        StringBuilder result = new StringBuilder();
        result.append(days).append(" day");
        if (days > 1) {
            result.append("s");
        }
        result.append(" ");
        result.append(hours).append(" hour");
        if (hours > 1) {
            result.append("s");
        }
        result.append(" ");
        result.append(minutes).append(" min");
        if (minutes > 1) {
            result.append("s");
        }
        return result.toString().trim();
    }

    @Override
    public void execute(File file, HttpServletRequest req, HttpServletResponse resp) {
    }

    @Override
    public void disable() {
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
