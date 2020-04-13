package codebal.catcher;

import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CcLogger {
    static Logger logger = Logger.getGlobal();

    static public void info(Class cls, Object msg){
        showMessage(cls, Level.INFO , msg);
    }

    static public void debug(Class cls, Object msg){
        showMessage(cls, Level.INFO , msg);
    }

    static public void warn(Class cls, Object msg){
        showMessage(cls, Level.WARNING, msg);
    }

    static public void error(Class cls, Object msg){
        showMessage(cls, Level.SEVERE , msg);
    }

    static void showMessage(Class cls, Level level, Object msg){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss");
        String date = format.format(System.currentTimeMillis());
        System.out.println(cls.getName() + " -- " + date + " [Catcher] DEBUG - " + msg);

        //logger.log(level, cls.getName() + " -- " + date + " [Catcher] DEBUG - " + msg);
    }
}
