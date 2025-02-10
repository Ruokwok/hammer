package cc.ruok.hammer.site;

import cc.ruok.hammer.Config;
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
        String help =   "\nUsage:  hmr [command]\n" +
                        "\nCommands:\n" +
                        "  run\t\tStart the hammer\n" +
                        "  stop\t\tStop the hammer\n" +
                        "  -v,version\tPrint hammer version information\n" +
                        "  -pl,plugins\tPrint installed plugin list\n";
        String echo = help;
        try {
            String url[] = req.getRequestURI().substring(1).split("/");

        } catch (Exception e) {
            echo = help;
        } finally {
            resp.getWriter().print(echo);
        }
    }

    @Override
    public void execute(File file, HttpServletRequest req, HttpServletResponse resp) throws IOException {
    }
}
