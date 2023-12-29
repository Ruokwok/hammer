package cc.ruok.hammer.error;

import cc.ruok.hammer.Logger;
import cc.ruok.hammer.site.WebSite;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

public class Http403Exception extends HttpException {

    public Http403Exception(WebSite site) {
        super(site);
    }

    @Override
    public String getPage() {
        try {
            String page = site.getErrorPage(403);
            if (page != null) return page;
            return IOUtils.toString(getClass().getResourceAsStream("/default_page/error_403.html"), "utf8");
        } catch (IOException e) {
            Logger.logException(e);
        }
        return null;
    }
}
