package cc.ruok.hammer.error;

import cc.ruok.hammer.site.WebSite;

public class HttpException extends Exception {

    WebSite site;
    int code = 500;

    public HttpException(WebSite site) {
        this.site = site;
    }

    public HttpException(WebSite site, int code) {
        this.site = site;
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public String getPage(){
        return null;
    }

}
