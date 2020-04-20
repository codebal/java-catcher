package org.codebal.cache;

import org.codebal.cache.logger.CcLogger;

import java.util.Date;
import java.util.function.Supplier;

public class CcRunner implements Runnable {

    Catcher catcher;
    Supplier<Object> supplier;

    public Date start_dt;
    public CcData ccData;

    public CcRunner(Catcher catcher, CcData ccData, Supplier<Object> supplier){
        this.catcher = catcher;
        this.ccData = ccData;
        this.supplier = supplier;
    }

    public boolean isTooLong(){
        boolean tooLong = System.currentTimeMillis() - this.start_dt.getTime() > 1000 * 10;
        if(tooLong){
            CcLogger.error(this.getClass(), "캐시 생성 너무 오래 걸림 : " + ccData.key);
        }
        return tooLong;
    }

    @Override
    public void run() {
        //CcLogger.debug("thread name : " + Thread.currentThread().getName());
        start_dt = new Date(System.currentTimeMillis());

        try{
            ccData.setData(supplier.get());
        }
        catch(Exception e){
            CcLogger.error(this.getClass(), "thread name : " + Thread.currentThread().getName());
            CcLogger.error(this.getClass(), e);
        }
        finally {
            catcher.cacheResourceSetter.apply(ccData);
            catcher.endCreatingCache(ccData);
            //CcLogger.debug(ccData);
        }
    }
}
