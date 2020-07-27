package com.codebal.cache.catcher;

import java.util.function.Function;
import java.util.function.Supplier;

import com.codebal.cache.catcher.logger.CacheLogger;

public class Catcher {

    static String ver = "0.2.0";

    static final int KEY_MAX_LENGTH = 200;

    private int waitCreateIntervalMs = 100; //캐시가 생성되기 기다리는 시간
    private int waitCreateRetryMaxCnt = 50; //캐시가 생성을 기다리는 최대 재시도 수

    //private int forceRefreshTimeoverMs = 1000 * 10; //리프래시 시간이 지난후에도 리프래시가 안되는경우, 강제로 리프래시를 하는 초과시간

    //int defaultRefreshSec = 60;
    //int defaultExpireSec = 60 * 5;

    boolean async = true;

    public enum _CacheAction{
        GET,
        WAIT,
        CREATE_AND_WAIT,
        GET_AND_CREATE_ASYNC
    }

    public class CacheAction{
        public boolean create;
        public boolean wait;
        public boolean async;

        public CacheAction(boolean create, boolean wait, boolean async){
            this.create = create;
            this.wait = wait;
            this.async = async;
        }

        @Override
        public String toString() {
            return "CacheAction [async=" + async + ", create=" + create + ", wait=" + wait + "]";
        }
        
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

//    public CacheData createCacheDataAsync(String key, Supplier<Object> supplier, Integer refresh_sec, Integer expire_sec, Boolean asyncUpdate, Boolean asyncNew){
//        CacheData cacheData = new CacheData(key, null, refresh_sec, expire_sec, asyncUpdate, asyncNew);
//        return createCacheDataAsync(cacheData, supplier);
//    }

    public CacheData createCacheDataSync(CacheData cacheData, Supplier<Object> supplier){
        try{
            Object data = supplier.get();
            cacheData.setData(data);
            endCreatingCache(cacheData);
        }
        catch(Exception e){
            cacheData = onCacheError(CacheError.make(e, cacheData));
        }
        return cacheData;
    }

    public CacheData createCacheDataAsync(CacheData cacheData, Supplier<Object> supplier){
        return cacheMaker.make(cacheData, supplier);
    }


    public CacheData getSetCacheData(String key, Supplier<Object> supplier, Integer refresh_sec, Integer expire_sec, Boolean asyncNew, Boolean asyncUpdate, Catcher.CacheCreateErrorHandle cacheCreateErrorHandle){
        CacheData cacheData = new CacheData(key, null, CacheData.Status.NEW, refresh_sec, expire_sec, asyncUpdate, asyncNew, cacheCreateErrorHandle);
        CacheData _oldCacheData = getCacheData(key);

        //System.out.println("-- new chacheData : " + cacheData);
        //System.out.println("-- old chacheData : " + _oldCacheData);

        cacheData.mergeOldCacheData(_oldCacheData);
        
        //System.out.println("-- merged chacheData : " + cacheData);

        cacheData.initDate(false, true);

        // CacheLogger.debug(this.getClass(), "status : " + cacheData.status);
        // CacheLogger.debug(this.getClass(), "cacheData : " + cacheData);

        // boolean needCreate = false;
        // boolean needWait = false;

        CacheAction action = getAction(cacheData);

        // if(CacheAction.WAIT.equals(action)){
        //     needWait = true;
        // }
        // else if(CacheAction.CREATE_AND_WAIT.equals(action) || CacheAction.GET_AND_CREATE_ASYNC.equals(action)){
        //     needCreate = true;

        //     // if(CacheAction.CREATE_AND_WAIT.equals(action)){ // cacheData is null
        //     //     cacheData = newCacheData;
        //     // }
        // }

        //System.out.println(action + " - " + Thread.currentThread().getName());

        if(action.create){ //캐시 생성을 해야 한다
            // boolean directCreating = false;

            //System.out.println("sync begin - " + Thread.currentThread().getName());

            synchronized (this){
                CacheData currentCacheData = getCacheData(key);
                if(currentCacheData != null){
                    cacheData = currentCacheData;
                }
                action = getAction(cacheData);

                //System.out.println("------ 다시 체크 : " + Thread.currentThread().getName());

                if(action.create){
                    //System.out.println("----- 생성 : " + Thread.currentThread().getName());
                    flagStartCreatingCache(cacheData);
                }
                else{
                    //System.out.println("----- 아니네 패스 - " + Thread.currentThread().getName());
                }


                //System.out.println(cacheData + "- " + Thread.currentThread().getName());
                // if(cacheData.needForceRefresh()){
                //     //문제가 생겨서 강제로 리프래시 해줘야 한다.
                //     cacheData.setCreating(false);
                // }
                // else{
                //     //System.out.println("상태바꼈나? / " + currentCacheData + "- " + Thread.currentThread().getName());
                //     if(currentCacheData != null)
                //     cacheData = currentCacheData;
                // }

                // if(!cacheData.isCreating()){
                //     directCreating = true;
                //     flagStartCreatingCache(cacheData);
                // }

                //System.out.println("sync 2 - " + Thread.currentThread().getName());
            }

            if(action.create){
                //System.out.println("----- 캐시생성 : " + action);
                if(action.async){ //비동기 캐시 생성
                    //System.out.println("----- 캐시생성 비동기 : " + action);
                    createCacheDataAsync(cacheData, supplier);
                }
                else{ //동기 캐시 생성
                    //System.out.println("----- 캐시생성 동기 : " + action);
                    cacheData = createCacheDataSync(cacheData, supplier);
                }
            }

            //System.out.println("sync end - " + Thread.currentThread().getName());

            // if(directCreating){
            //     // if(CacheAction.GET_AND_CREATE_ASYNC.equals(action) && !cacheData.asyncUpdate){ //캐시 리프래시, asyncUpdate = false
            //     //     async = false;
            //     // }
            //     // else if(CacheAction.CREATE_AND_WAIT.equals(action) && !cacheData.asyncNew){ //캐시 신규 생성, asyncNew = false
            //     //     async = false;
            //     // }

            //     if(action.async){ //비동기 캐시 생성
            //         createCacheDataAsync(cacheData, supplier);
            //     }
            //     else{ //동기 캐시 생성
            //         cacheData = createCacheDataSync(cacheData, supplier);
            //     }
            // }
            // else{
            //     action = getAction(cacheData);
            //     // if(CacheAction.WAIT.equals(action)){
            //     //     needWait = true;
            //     // }
            // }

        }

        if(action.wait){
            cacheData = waitCreateCache(cacheData.key);
        }

        return cacheData;
    }

    public CacheAction _getAction(String key){
        CacheData cacheData = getCacheData(key);

        return getAction(cacheData);
    }

    public CacheAction getAction(CacheData cacheData){
        // boolean action_get = false;
        // boolean action_createAndWait = false;

        boolean need_create = false;
        boolean need_wait = false;
        boolean need_async = true;

        if(
            cacheData.isNew()
            || (cacheData.isNormal() && cacheData.needRefresh())
            || cacheData.needForceRefresh()
            ){
            need_create = true;
        }

        if( 
            (cacheData.isNew() && !cacheData.asyncNew)
            || (cacheData.isNewCreating() && !cacheData.asyncNew)
            || (cacheData.isCreating() && !cacheData.asyncUpdate)
            ){
            need_wait = true;
        }

        if(need_create && need_wait){ //최초 캐시 생성
            if(!cacheData.asyncNew)
                need_async = false;
         }
        else if(need_create){ //캐시 갱신
            if(!cacheData.asyncUpdate)
                need_async = false;
        }

        CacheAction action = new CacheAction(need_create, need_wait, need_async);

        // CacheLogger.trace(this.getClass(), "before action select: " + cacheData);
        // CacheLogger.trace(this.getClass(), "result action : " + action);
        // CacheLogger.trace(this.getClass(), "cacheData.needRefresh(): " + cacheData.needRefresh());
        // CacheLogger.trace(this.getClass(), "cacheData.create_dt: " + cacheData.create_dt);
        // CacheLogger.trace(this.getClass(), "cacheData.update_dt: " + cacheData.update_dt);

        return action;

        // //------------- 캐시 생성 & 대기 ---------------
        // if(cacheData.isNew() && !cacheData.asyncNew){
        //     action_createAndWait = true;
        // }
        // else if(cacheData.isNormal() && cacheData.needRefresh() && !cacheData.asyncUpdate){
        //     action_createAndWait = true;
        // }

        // if(action_createAndWait){
        //     return CacheAction.CREATE_AND_WAIT;
        // }


        // //------------- 캐시 갱신 ---------------
        // if(!cacheData.isCreating() && cacheData.needRefresh())
        //     need_create = true;

        // if(cacheData.needForceRefresh())
        //     need_create = true;

        // if(need_create)
        //     return CacheAction.GET_AND_CREATE_ASYNC;

        // //캐시가 생성중인데 데이터는 없고 asyncNew이 false 일때
        // if(cacheData.isCreating()){
        //     if(cacheData.isNew() && !cacheData.asyncNew){
        //         needWait = true;
        //     }
        //     else if(!cacheData.asyncUpdate){
        //         needWait = true;
        //     }
        // }

        // //------------- 캐시 생성 대기 ---------------
        // if(cacheData.isCreating() && cacheData.asyncUpdate){
        //     needWait = true;
        // }

        // if(needWait)
        //     return CacheAction.WAIT;




        // if(cacheData != null){
        // }
        // else{
        //     return CacheAction.CREATE_AND_WAIT;
        // }

        // //------------- 캐시만 반환 ---------------
        // return CacheAction.GET;
    }

    public <T> T getSet(String key, Supplier<Object> supplier, Integer refresh_sec, Integer expire_sec, Boolean asyncNew, Boolean asyncUpdate, Catcher.CacheCreateErrorHandle cacheCreateErrorHandle) {
        CacheData cacheData = getSetCacheData(key, supplier, refresh_sec, expire_sec, asyncUpdate, asyncNew, cacheCreateErrorHandle);
        if(cacheData == null)
            return null;
        return (T) cacheData.getData();
    }

    public void _flagStartCreatingCache(String key, int refresh_sec, int expire_sec){
        CacheData cacheData = getCacheData(key);
        if(cacheData == null)
            cacheData = new CacheData(key, null, CacheData.Status.CREATING, refresh_sec, expire_sec, null, null, null);

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

    public CacheData endCreatingCache(CacheData cacheData){
        cacheData.initDate(true, true);
        cacheData.status = CacheData.Status.NORMAL;
        setCacheData(cacheData);
        return cacheData;

        // CacheData newCacheData = null;
        // if(cacheData != null){
        //     newCacheData = new CacheData(cacheData.key, cacheData.getData(), CacheData.Status.NORMAL, cacheData.getRefresh_sec(), cacheData.getExpire_sec(), cacheData.asyncUpdate, cacheData.asyncNew, cacheData.cacheCreateErrorHandle);
        //     setCacheData(newCacheData);
        // }

        // return newCacheData;
    }

    public CacheData extendCacheTime(CacheData cacheData){
        CacheData curCacheData = getCacheData(cacheData.key);
        if(curCacheData != null){
            CacheData endCacheData = endCreatingCache(curCacheData);
            CacheLogger.debug(this.getClass(), "extending cache time is success. cache(" + cacheData.key + ") : " + endCacheData);
            return curCacheData;
        }
        else{
            CacheLogger.error(this.getClass(), "extending cache time is fail. cache(" + cacheData.key + ") is null.");
            return cacheData;
        }
    }

    public CacheData waitCreateCache(String key) {

        CacheData cacheData;
        int tryCnt = 0;
        while(true){
            tryCnt++;
            cacheData = getCacheData(key);
            if(cacheData != null && !cacheData.isBusyNow()){
                return cacheData;
            }
            //CacheLogger.trace(this.getClass(), "wait for creating cache / key:" + key + " --|");
            //CacheLogger.trace(this.getClass(), "wait for creating cache : " + cacheData);
            

            if(tryCnt > waitCreateRetryMaxCnt){
                CacheLogger.error(this.getClass(), "waiting for cache(" + key + ") creating overtime / count:" + tryCnt + ", key:" + key + " --|" );
                onCacheError(CacheError.make("wait overtime exception", cacheData));
                //flagFinishCreatingCache(cacheData);
                return cacheData;
            }

            try{
                CacheLogger.trace(this.getClass(), "thread [" + Thread.currentThread().getName() + "] sleep (" + waitCreateIntervalMs + "/" + tryCnt + ") waiting for cache(" + key + ") creating ");
                Thread.sleep(waitCreateIntervalMs);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    static public String getLimitCacheKey(String key){
        if(key.length() > KEY_MAX_LENGTH){
            return key.substring(0, KEY_MAX_LENGTH-1);
        }
        else{
            return key;
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

    public CacheData onCacheError(CacheError cacheError){
        CacheData cacheData = cacheError.getCacheData();
        
        cacheError.getException().printStackTrace();
        CacheLogger.error(Catcher.class, cacheError.getException());

        if(cacheData.getCacheCreateErrorHandle().equals(CacheCreateErrorHandle.REUSE)){
            cacheData = extendCacheTime(cacheData);
            return cacheData;
        }
        else if(cacheData.getCacheCreateErrorHandle().equals(CacheCreateErrorHandle.CUSTOM)){
            cacheData = errorHandleWithCatcherSignal(cacheError);
            endCreatingCache(cacheData);
            return cacheData;
        }
        else{
            cacheData.setData(null);
            cacheData = extendCacheTime(cacheData);
        }

        return cacheData;
    }

    public CacheData errorHandleWithCatcherSignal(CacheError cacheError){
        if(catcherSignal != null){
            CacheData handledCacheData = catcherSignal.cacheCreateCustomErrorHandler(cacheError);
            return handledCacheData;
        }
        return cacheError.getCacheData();
    }

    public int getWaitCreateIntervalMs() {
        return waitCreateIntervalMs;
    }

    public void setWaitCreateIntervalMs(int waitCreateIntervalMs) {
        this.waitCreateIntervalMs = waitCreateIntervalMs;
    }

    public int getWaitCreateRetryMaxCnt() {
        return waitCreateRetryMaxCnt;
    }

    public void setWaitCreateRetryMaxCnt(int waitCreateRetryMaxCnt) {
        this.waitCreateRetryMaxCnt = waitCreateRetryMaxCnt;
    }
}
