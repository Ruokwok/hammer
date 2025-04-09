package cc.ruok.hammer.error;

import cc.ruok.hammer.Logger;
import cc.ruok.hammer.site.WebSite;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

public class Http404Exception extends HttpException {

    public Http404Exception(WebSite site) {
        super(site);
        this.code = 404;
    }

    @Override
    public String getPage() {
        try {
            String page = site.getErrorPage(404);
            if (page != null) return page;
            return IOUtils.toString(getClass().getResourceAsStream("/default_page/error_404.html"), "utf8");
        } catch (IOException e) {
            Logger.logException(e);
        }
        return null;
    }
}
