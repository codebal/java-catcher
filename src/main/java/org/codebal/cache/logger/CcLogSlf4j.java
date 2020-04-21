package org.codebal.cache.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CcLogSlf4j{
    static void log(Class cls, CcLogger.Level level, String msg){
        Logger logger = LoggerFactory.getLogger(cls);
        if(CcLogger.Level.DEBUG.equals(level)){
            logger.debug(msg);
        }
        else if(CcLogger.Level.INFO.equals(level)){
            logger.info(msg);
        }
        else if(CcLogger.Level.ERROR.equals(level)){
            logger.error(msg);
        }
        else if(CcLogger.Level.WARNING.equals(level)){
            logger.warn(msg);
        }
    }
}
