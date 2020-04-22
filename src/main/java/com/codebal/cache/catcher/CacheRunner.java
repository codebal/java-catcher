package com.codebal.cache.catcher;

import com.codebal.cache.catcher.logger.CacheLogger;

import java.util.Date;
import java.util.function.Supplier;

public class CacheRunner implements Runnable {

    Catcher catcher;
    Supplier<Object> supplier;

    public Date start_dt;
    public CacheData cacheData;

    public CacheRunner(Catcher catcher, CacheData cacheData, Supplier<Object> supplier){
        this.catcher = catcher;
        this.cacheData = cacheData;
        this.supplier = supplier;
    }

    public boolean isTooLong(){
        boolean tooLong = System.currentTimeMillis() - this.start_dt.getTime() > 1000 * 10;
        if(tooLong){
            CacheLogger.error(this.getClass(), "캐시 생성 너무 오래 걸림 : " + cacheData.key);
        }
        return tooLong;
    }

    @Override
    public void run() {
        //CacheLogger.debug("thread name : " + Thread.currentThread().getName());
        start_dt = new Date(System.currentTimeMillis());

        try{
            cacheData.setData(supplier.get());
        }
        catch(Exception e){
            CacheLogger.error(this.getClass(), "thread name : " + Thread.currentThread().getName());
            CacheLogger.error(this.getClass(), e);
        }
        finally {
            catcher.cacheResourceSetter.apply(cacheData);
            catcher.endCreatingCache(cacheData);
            //CacheLogger.debug(cacheData);
        }
    }
}
