package cc.ruok.hammer;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;

public class Logger {

    private static final Log log = LogFactory.get();

    public static void logException(Exception e) {
        log.error(e);
    }

    public static void info(String str) {
        log.info(str);
    }

    public static void warning(String str) {
        log.warn(str);
    }

    public static void debug(String str) {
        log.debug(str);
    }

    public static void error(String str) {
        log.error(str);
    }

    public static void trace(String str) {
        log.trace(str);
    }

}
