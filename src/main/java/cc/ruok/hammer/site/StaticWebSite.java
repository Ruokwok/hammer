package cc.ruok.hammer.site;

import cc.ruok.hammer.*;
import cc.ruok.hammer.error.Http403Exception;
import cc.ruok.hammer.error.Http404Exception;
import cc.ruok.hammer.error.Http500Exception;
import cc.ruok.hammer.error.HttpException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class StaticWebSite extends WebSite {

    public StaticWebSite(Config config) {
        super(config);
    }

    @Override
    public void execute(File file, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("utf8");
        String type = WebServlet.getFileType(file.getName());
        resp.setHeader("Content-Type", type);
        if (type.equals("application/octet-stream")) {
            resp.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
        }
//            resp.getWriter().println(FileUtils.readFileToString(file, "utf-8"));
        FileInputStream inputStream = new FileInputStream(file);
        IOUtils.write(inputStream.readAllBytes(), resp.getOutputStream());
        inputStream.close();
    }
}
