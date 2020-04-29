package com.codebal.cache.catcher.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindSlf4j {
    static void log(Class cls, CacheLogger.Level level, String msg){
        Logger logger = LoggerFactory.getLogger(cls);
        if(CacheLogger.Level.TRACE.equals(level)){
            logger.trace(msg);
        }
        else if(CacheLogger.Level.DEBUG.equals(level)){
            logger.debug(msg);
        }
        else if(CacheLogger.Level.INFO.equals(level)){
            logger.info(msg);
        }
        else if(CacheLogger.Level.ERROR.equals(level)){
            logger.error(msg);
        }
        else if(CacheLogger.Level.WARNING.equals(level)){
            logger.warn(msg);
        }
    }
}
