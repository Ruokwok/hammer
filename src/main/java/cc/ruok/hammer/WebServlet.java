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
        if (filetype.endsWith(".apk")) return "application/vnd.android.package-archive";
        if (filetype.endsWith(".css")) return "text/css";
        if (filetype.endsWith(".gif")) return "image/gif";
        if (filetype.endsWith(".htm")) return "text/html";
        if (filetype.endsWith(".html")) return "text/html";
        if (filetype.endsWith(".htx")) return "text/html";
        if (filetype.endsWith(".hsp")) return "text/html";
        if (filetype.endsWith(".ico")) return "image/x-icon";
        if (filetype.endsWith(".img")) return "application/x-img";
        if (filetype.endsWith(".ipa")) return "application/vnd.iphone";
        if (filetype.endsWith(".jpeg")) return "image/jpeg";
        if (filetype.endsWith(".jpg")) return "image/jpeg";
        if (filetype.endsWith(".js")) return "application/x-javascript";
        if (filetype.endsWith(".pdf")) return "application/pdf";
        if (filetype.endsWith(".png")) return "image/png";
        if (filetype.endsWith(".ppt")) return "application/vnd.ms-powerpoint";
        if (filetype.endsWith(".txt")) return "text/plain";
        if (filetype.endsWith(".doc")) return "application/msword";
        if (filetype.endsWith(".docx")) return "application/msword";
        if (filetype.endsWith(".xhtml")) return "text/html";
        if (filetype.endsWith(".xls")) return "application/vnd.ms-excel";
        if (filetype.endsWith(".xlsx")) return "application/vnd.ms-excel";
        if (filetype.endsWith(".xml")) return "text/xml";
        return "application/octet-stream";
    }
}
