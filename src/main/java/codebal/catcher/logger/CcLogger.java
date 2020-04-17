package codebal.catcher.logger;

import java.text.SimpleDateFormat;
import java.util.logging.Logger;

public class CcLogger {
    public enum Level {
        DEBUG,
        INFO,
        WARNING,
        ERROR
    }

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

    static void showLog(Class cls, Level level, Object msg){

        Logger logger = Logger.getLogger(cls.getName());

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss");
        String date = format.format(System.currentTimeMillis());
        //System.out.println(cls.getName() + " -- " + date + " [Catcher] DEBUG - " + msg);

        //logger.log(level, cls.getName() + " -- " + date + " [Catcher] DEBUG - " + msg);

        String msgStr = (String)msg;

        basicLog(cls, level, msgStr);

//        if(isExistClass("org.slf4j.Logger")){
//            CcLogSlf4j.log(cls, level, msgStr);
//        }
//        else{
//            basicLog(cls, level, msgStr);
//        }
    }

    static void basicLog(Class cls, Level level, Object msg){
        if(level.equals(Level.DEBUG)){
            if(!isDebugMode()) return;
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss");
        String date = format.format(System.currentTimeMillis());
        System.out.println(date + " - " + cls.getName() + " - " + msg);
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
