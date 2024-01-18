package cc.ruok.hammer;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.log.StaticLog;

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

}
