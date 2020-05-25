package com.codebal.cache.catcher;

import com.codebal.cache.catcher.logger.CacheLogger;

import java.util.function.Function;
import java.util.function.Supplier;

public class Catcher {

    private int waitCreateIntervalMs = 500; //캐시가 생성되기 기다리는 시간
    private int waitCreateRetryMaxCnt = 10; //캐시가 생성을 기다리는 최대 재시도 수

    private int forceRefreshTimeoverMs = 1000 * 10; //리프래시 시간이 지난후에도 리프래시가 안되는경우, 강제로 리프래시를 하는 초과시간

    int defaultRefreshSec = 60;
    int defaultExpireSec = 60 * 5;

    boolean async = true;

    public enum Action{
        GET_CACHE_ONLY,
        WAIT_CACHE_CREATE,
        DIRECT_NEW_CACHE,
        DIRECT_REFRESH_CACHE
    }

    public enum CacheCreateErrorHandle {
        NULL,
        REUSE,
        CUSTOM
    }

    Function<CacheData, Boolean> cacheResourceSetter;
    Function<Object, CacheData> cacheResourceGetter;

    CacheMaker cacheMaker;

    ICatcherSignal catcherSignal = null;

    public Catcher(Function<CacheData, Boolean> cacheResourceSetter, Function<Object, CacheData> cacheResourceGetter){
        this.cacheResourceSetter = cacheResourceSetter;
        this.cacheResourceGetter = cacheResourceGetter;

        cacheMaker = new CacheMaker(this);
    }

    public Catcher(ICatcherSignal catcherSignal){
        this.catcherSignal = catcherSignal;

        cacheMaker = new CacheMaker(this);
    }

    public boolean setCacheData(CacheData cacheData){
        //CacheLogger.debug("--- 캐시입력 : " + cacheData + " ---");
        if(catcherSignal != null)
            return catcherSignal.cacheResourceSetter(cacheData);
        else
            return cacheResourceSetter.apply(cacheData);
    }

    public CacheData getCacheData(String key){
        try{
            if(catcherSignal != null){
                return catcherSignal.cacheResourceGetter(key);
            }
            else{
                return cacheResourceGetter.apply(key);
            }
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
//        key = CacheData.getLimitCacheKey(key);
//        try{
//            SerializableDocument couchdata = bucket.get(key, SerializableDocument.class);
//            if(couchdata == null || couchdata.content() == null){
//                //logger.debug("--- 캐시없음 : " + key + " ---");
//                return null;
//            }
//            else{
//                try{
//                    CacheData cacheData = (CacheData)couchdata.content();
//                    //logger.debug("--- 캐시있음 : " + key + " ---");
//                    return cacheData;
//                }
//                catch(Exception e){
//                    showLog("--- 캐시가 있지만 타입이 틀려서 변환 오류 : " + key + " ---");
//                    e.printStackTrace();
//                    return null;
//                }
//            }
//        }
//        catch(Exception e){
//            showLog("--- 캐시 key 디코드 오류 : " + key + " ---");
//            e.printStackTrace();
//            return null;
//        }
    }

//    public CacheData getSetCacheData(CacheData cacheData, Supplier<CacheData> supplier){
//        CacheData currentCacheData = this.getCacheData(cacheData.key);
//
//        boolean needCreate = false;
//        if(cacheData != null){
//            if(!cacheData.isCreating() && cacheData.needRefresh())
//                needCreate = true;
//        }
//        else{
//            needCreate = true;
//        }
//
//        if(needCreate){
//            if(cacheData.asyncRefresh){
//                currentCacheData = createCacheData(cacheData.key, ()->{
//                    CacheData newCacheData = supplier.get();
//                    setCacheData(cacheData);
//                    return cacheData;
//                });
//            }
//            else{
//                createCacheData(cacheData.key, ()->{
//                    this.setCacheData(supplier);
//                    return null;
//                });
//            }
//        }
//        else{
//            if(wait && currentCacheData.isCreating())
//                waitCreateCache(cacheData.key);
//        }
//
//        return currentCacheData;
//    }

    public CacheData updateCacheData(CacheData cacheData, boolean nullable){
        CacheData targetCacheData = getCacheData(cacheData.key);
        if(targetCacheData == null)
            targetCacheData = cacheData;
        else{
            if(nullable || cacheData.getData() != null)
                targetCacheData.setData(cacheData.getData());
            if(cacheData.status != null)
                targetCacheData.status = cacheData.status;
        }

        //여기 하다가 말았다.
        return cacheData;
    }

    public <T> T get(String key){
        CacheData cacheData = getCacheData(key);
        if(cacheData == null)
            return null;
        else
            return cacheData.getData();
    }

//    public CacheData createCacheDataAsync(String key, Supplier<Object> supplier, Integer refresh_sec, Integer expire_sec, Boolean asyncRefresh, Boolean richStart){
//        CacheData cacheData = new CacheData(key, null, refresh_sec, expire_sec, asyncRefresh, richStart);
//        return createCacheDataAsync(cacheData, supplier);
//    }

    public CacheData createCacheDataSync(CacheData cacheData, Supplier<Object> supplier){
        try{
            cacheData.setData(supplier.get());
        }
        catch(Exception e){
            e.printStackTrace();
            CacheLogger.error(Catcher.class, e);
            if(cacheData.getCacheCreateErrorHandle().equals(CacheCreateErrorHandle.REUSE)){
                cacheData = extendCacheTime(cacheData);
            }
            else if(cacheData.getCacheCreateErrorHandle().equals(CacheCreateErrorHandle.CUSTOM)){
                cacheData = onCacheCreateError(CacheError.make(e, cacheData));
            }
        }
        //setCacheData(cacheData);
        endCreatingCache(cacheData);

        return cacheData;
    }

    public CacheData createCacheDataAsync(CacheData cacheData, Supplier<Object> supplier){
        return cacheMaker.make(cacheData, supplier);
    }


    public CacheData getSetCacheData(String key, Supplier<Object> supplier, Integer refresh_sec, Integer expire_sec, Boolean asyncRefresh, Boolean richStart){
        CacheData newCacheData = new CacheData(key, null, CacheData.Status.NEW, refresh_sec, expire_sec, asyncRefresh, richStart);
        CacheData cacheData = getCacheData(key);

        boolean needCreate = false;
        boolean needWait = false;

        Action action = getAction(cacheData);
        if(Action.WAIT_CACHE_CREATE.equals(action)){
            needWait = true;
        }
        else if(Action.DIRECT_NEW_CACHE.equals(action) || Action.DIRECT_REFRESH_CACHE.equals(action)){
            needCreate = true;

            if(Action.DIRECT_NEW_CACHE.equals(action)){ // cacheData is null
                cacheData = newCacheData;
            }
        }

        //System.out.println(action + " - " + Thread.currentThread().getName());

        if(needCreate){ //캐시 생성을 해야 한다
            boolean directCreating = false;

            //System.out.println("sync begin - " + Thread.currentThread().getName());

            synchronized (this){
                //System.out.println("sync 1 - " + Thread.currentThread().getName());
                //System.out.println(cacheData + "- " + Thread.currentThread().getName());
                if(cacheData.needForceRefresh()){
                    //문제가 생겨서 강제로 리프래시 해줘야 한다.
                    cacheData.setCreating(false);
                }
                else{
                    CacheData currentCacheData = getCacheData(key);
                    //System.out.println("상태바꼈나? / " + currentCacheData + "- " + Thread.currentThread().getName());
                    if(currentCacheData != null)
                        cacheData = currentCacheData;
                }

                if(!cacheData.isCreating()){
                    directCreating = true;
                    flagStartCreatingCache(cacheData);
                }

                //System.out.println("sync 2 - " + Thread.currentThread().getName());
            }

            //System.out.println("sync end - " + Thread.currentThread().getName());

            if(directCreating){
                boolean async = true;
                if(Action.DIRECT_REFRESH_CACHE.equals(action) && !newCacheData.asyncRefresh){ //캐시 리프래시, asyncRefresh = false
                    async = false;
                }
                else if(Action.DIRECT_NEW_CACHE.equals(action) && newCacheData.richStart){ //캐시 신규 생성, richStart = true
                    async = false;
                }

                if(async){ //비동기 캐시 생성
                    createCacheDataAsync(newCacheData, supplier);
                }
                else{ //동기 캐시 생성
                    cacheData = createCacheDataSync(newCacheData, supplier);
                }
            }
            else{
                action = getAction(cacheData);
                if(Action.WAIT_CACHE_CREATE.equals(action)){
                    needWait = true;
                }
            }

        }

        if(needWait){
            cacheData = waitCreateCache(cacheData.key);
        }

        return cacheData;
    }

    public Action getAction(String key){
        CacheData cacheData = getCacheData(key);

        return getAction(cacheData);
    }

    public Action getAction(CacheData cacheData){
        boolean needCreate = false;
        boolean needWait = false;
        if(cacheData != null){
            if(!cacheData.isCreating() && cacheData.needRefresh())
                needCreate = true;

            if(cacheData.needForceRefresh())
                needCreate = true;

            if(needCreate)
                return Action.DIRECT_REFRESH_CACHE;

            //캐시가 생성중인데 데이터는 없고 richStart이 false 일때
            if(cacheData.isCreating()){
                if(cacheData.status.equals(CacheData.Status.NEW) && cacheData.richStart){
                    needWait = true;
                }
                else if(!cacheData.asyncRefresh){
                    needWait = true;
                }
            }

            if(needWait)
                return Action.WAIT_CACHE_CREATE;
        }
        else{
            return Action.DIRECT_NEW_CACHE;
        }

        return Action.GET_CACHE_ONLY;
    }

    public <T> T getSet(String key, Supplier<Object> supplier, Integer refresh_sec, Integer expire_sec, Boolean asyncRefresh, Boolean richStart){
        CacheData cacheData = getSetCacheData(key, supplier, refresh_sec, expire_sec, asyncRefresh, richStart);
        if(cacheData == null)
            return null;
        return (T) cacheData.getData();
    }

    public void flagStartCreatingCache(String key, int refresh_sec, int expire_sec){
        CacheData cacheData = getCacheData(key);
        if(cacheData == null)
            cacheData = new CacheData(key, null, CacheData.Status.CREATING, refresh_sec, expire_sec, null, null);

        flagStartCreatingCache(cacheData);
    }

    public void flagStartCreatingCache(CacheData cacheData){
        cacheData.setCreating(true);
        setCacheData(cacheData);
    }

    public void flagFinishCreatingCache(CacheData cacheData){
        cacheData.status = CacheData.Status.NORMAL;
        cacheData.setCreating(false);
        setCacheData(cacheData);
    }

    public void endCreatingCache(String key){
        endCreatingCache(getCacheData(key));
    }

    public void endCreatingCache(CacheData cacheData){
        if(cacheData != null){
            CacheData newCacheData = new CacheData(cacheData.key, cacheData.getData(), CacheData.Status.NORMAL, cacheData.getRefresh_sec(), cacheData.getExpire_sec(), cacheData.asyncRefresh, cacheData.richStart);
            setCacheData(newCacheData);
        }
    }

    public CacheData extendCacheTime(CacheData cacheData){
        CacheData curCacheData = getCacheData(cacheData.key);
        if(curCacheData != null){
            endCreatingCache(curCacheData);
            return curCacheData;
        }
        else{
            CacheLogger.error(this.getClass(), "extending cache time is fail. cache(" + cacheData.key + ") is null.");
            return cacheData;
        }
    }

    public <T> T createCacheData(String key, Supplier<T> supplier){
        T result = null;

        //logger.debug("-------- begin create cache / key : " + key + "--------");
        try{
            result = supplier.get();
        }
        catch(Exception e){
            endCreatingCache(key);
            CacheLogger.error(this.getClass(), "error createCacheData / key:" + key);
            e.printStackTrace();
        }
        finally {
            //logger.debug("-------- end create cache / key : " + key + "--------");
        }

        return result;
    }

    public CacheData waitCreateCache(String key) {

        CacheData cacheData;
        int tryCnt = 0;
        while(true){
            tryCnt++;
            cacheData = getCacheData(key);
            if(cacheData != null && !cacheData.isCreating()){
                return cacheData;
            }
            CacheLogger.trace(this.getClass(), "wait for creating cache / key:" + key + " ---");

            if(tryCnt > waitCreateRetryMaxCnt){
                CacheLogger.error(this.getClass(), "cache waiting too long / count:" + tryCnt + ", key:" + key + " ---" );
                flagFinishCreatingCache(cacheData);
                return cacheData;
            }

            try{
                Thread.sleep(waitCreateIntervalMs);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public void remove(String key){

    }

    public Function<CacheData, Boolean> getCacheResourceSetter() {
        return cacheResourceSetter;
    }

    public Function<Object, CacheData> getCacheResourceGetter() {
        return cacheResourceGetter;
    }

    public ICatcherSignal getCatcherSignal() {
        return catcherSignal;
    };

    public CacheData onCacheCreateError(CacheError cacheError){
        if(catcherSignal != null){
            CacheData handledCacheData = catcherSignal.cacheCreateCustomErrorHandler(cacheError);
            return handledCacheData;
        }
        return cacheError.getCacheData();
    }
}
