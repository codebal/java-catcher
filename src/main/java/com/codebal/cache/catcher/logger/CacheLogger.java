package com.codebal.cache.catcher.logger;

import java.text.SimpleDateFormat;

public class CacheLogger {
    public enum Level {
        TRACE,
        DEBUG,
        INFO,
        WARNING,
        ERROR
    }

    static Exception exception;
    static String msgStr;

    static public void trace(Class cls, Object msg){
        showLog(cls, Level.TRACE , msg);
    };

    static public void info(Class cls, Object msg){
        showLog(cls, Level.INFO , msg);
    }

    static public void debug(Class cls, Object msg){
        showLog(cls, Level.DEBUG , msg);
    }

    static public void warn(Class cls, Object msg){
        showLog(cls, Level.WARNING, msg);
    }

    static public void error(Class cls, Object msg){
        showLog(cls, Level.ERROR , msg);
    }

    static public boolean isDebugMode(){
        boolean isDebug =
                java.lang.management.ManagementFactory.getRuntimeMXBean().
                        getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
        return isDebug;
    }

    static String getStringFromException(Exception e){
        String rtn = null;
        if(e != null && e.getStackTrace().length > 0){
            for(StackTraceElement ste : e.getStackTrace()){
                if(rtn == null) rtn = "";
                if(rtn != null) rtn += "\n\t";
                    rtn += ste.toString();
            }
        }
        return rtn;
    }

    static void showLog(Class cls, Level level, Object msg){

        msgStr = null;
        exception = null;
        if(msg instanceof Exception){
            exception = (Exception)msg;
            msgStr = getStringFromException(exception);
        }
        else{
            msgStr = (String)msg;
        }

//        basicLog(cls, level, msgStr);

        if(isExistClass("_org.slf4j.Logger")){
            BindSlf4j.log(cls, level, msgStr);
        }
        else if(isExistClass("_org.apache.log4j.Logger")){
            BindLog4j.log(cls, level, msg);
        }
        else{
            basicLog(cls, level, msg);
        }
    }

    static void basicLog(Class cls, Level level, Object msg){
        if(level.equals(Level.DEBUG) || level.equals(Level.TRACE)){
            if(!isDebugMode()) return;
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String date = format.format(System.currentTimeMillis());
        System.out.println(date + " [" + Thread.currentThread().getName() + "] - " + cls.getName() + " - " + msg);
    }

    static boolean isExistClass(String className){
        try{
            Class.forName(className);
            return true;
        }catch(ClassNotFoundException e){
            return false;
        }
    }
}
