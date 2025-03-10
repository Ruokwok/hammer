package cc.ruok.hammer;

import cc.ruok.hammer.site.WebSite;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@MultipartConfig
public class WebServlet extends HttpServlet {

    private WebServer server = WebServer.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String host = req.getHeader("Host");
        resp.addHeader("Server", "hammer/" + Hammer.getVersion());
        if (host.contains("]:")) host = host.substring(0, host.indexOf("]:") + 1);
        int i = host.indexOf(":");
        if (i >= 0 && !host.contains("[")) host = host.substring(0, i);
        WebSite site = server.getWebSite(host);
        if (site == null) {
            resp.getWriter().println(WebSite.notSite(host));
        } else {
            site.handler(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }



    public static String getFileType(String filetype) {
        String type = "application/octet-stream";
        if (filetype.contains(".")) {
            String[] split = filetype.split("\\.");
            filetype = split[split.length - 1];
            if (Hammer.config.scriptFileTypes.contains(filetype)) return "text/html";
            if (Hammer.config.fileHeader.containsKey(filetype)) {
                return Hammer.config.fileHeader.get(filetype);
            }
            return type;
        }
        return type;
    }
}
