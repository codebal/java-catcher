package com.codebal.cache.catcher.logger;

public interface ICacheLogBinder {
    void trace(Class cls, String msg);
    void debug(Class cls, String msg);
    void info(Class cls, String msg);
    void error(Class cls, String msg);
}
