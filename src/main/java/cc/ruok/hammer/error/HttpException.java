package cc.ruok.hammer.error;

import cc.ruok.hammer.site.WebSite;

public abstract class HttpException extends Exception {

    WebSite site;

    public HttpException(WebSite site) {
        this.site = site;
    }

    public abstract String getPage();

}
