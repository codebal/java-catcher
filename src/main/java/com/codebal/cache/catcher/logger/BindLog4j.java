package com.codebal.cache.catcher.logger;

import org.apache.log4j.Logger;

public class BindLog4j {
    static void log(Class cls, CacheLogger.Level level, Object msg) {
        if (CacheLogger.Level.TRACE.equals(level)) {
            Logger.getLogger(cls).trace(msg);
        }
        else if (CacheLogger.Level.DEBUG.equals(level)) {
            Logger.getLogger(cls).debug(msg);
        }
        else if (CacheLogger.Level.INFO.equals(level)) {
            Logger.getLogger(cls).info(msg);
        }
        else if (CacheLogger.Level.ERROR.equals(level)) {
            Logger.getLogger(cls).error(msg);
        }
        else if (CacheLogger.Level.WARN.equals(level)) {
            Logger.getLogger(cls).warn(msg);
        }
    }
}
