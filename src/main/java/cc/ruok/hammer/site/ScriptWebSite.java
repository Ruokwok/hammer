package cc.ruok.hammer.site;

import cc.ruok.hammer.Config;
import cc.ruok.hammer.Logger;
import cc.ruok.hammer.WebServlet;
import cc.ruok.hammer.engine.Engine;
import cc.ruok.hammer.engine.EngineRequest;
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

public class ScriptWebSite extends WebSite {

    public ScriptWebSite(Config config) {
        super(config);
    }

    @Override
    public void handler(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            long start = System.currentTimeMillis();
            resp.setCharacterEncoding("utf8");
            File file = getFile(req);
            String type = WebServlet.getFileType(file.getName());
            resp.setHeader("Content-Type", type);
            if (type.equals("application/octet-stream")) {
                resp.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
            }
            if (file.getName().endsWith(".hsp")) {
                EngineRequest er = EngineRequest.createEngineRequest(req);
                String script = FileUtils.readFileToString(file, "utf-8");
                Engine e = new Engine(script, er, req, resp.getWriter(), this);
                long end = System.currentTimeMillis();
                resp.getWriter().println(e.execute());
                Logger.info("[" + getName() + "][" + req.getMethod() + "][" + resp.getStatus() + "]" +
                        req.getRemoteAddr() +
                        " - " + req.getRequestURI() + "(" + (end - start) + "ms)");

            } else {
                FileInputStream inputStream = new FileInputStream(file);
                IOUtils.write(inputStream.readAllBytes(), resp.getOutputStream());
                inputStream.close();
            }
        } catch (Http403Exception e) {
            resp.setStatus(403);
            resp.getWriter().println(e.getPage());
        } catch (Http404Exception e) {
            resp.setStatus(404);
            resp.getWriter().println(e.getPage());
        } catch (HttpException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            Logger.logException(e);
            resp.setStatus(500);
            resp.getWriter().println(new Http500Exception(this).getPage());
        }
    }

    private File getFile(HttpServletRequest request) throws HttpException {
        String url = request.getServletPath();
        File file = new File(config.path + url);
        if (!file.exists()) throw new Http404Exception(this);
        if (file.exists() && file.isDirectory()) {
            File page = new File(file + "/index.hs");
            if (!page.exists()) page = new File(file + "/index.hsp");
            if (!page.exists()) page = new File(file + "/index.html");
            if (!page.exists()) throw new Http403Exception(this);
            return page;
        }
        return file;
    }
}
