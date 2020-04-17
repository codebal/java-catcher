package codebal.catcher.logger;

public interface ICcLogBinder {
    void trace(Class cls, String msg);
    void debug(Class cls, String msg);
    void info(Class cls, String msg);
    void error(Class cls, String msg);
}
