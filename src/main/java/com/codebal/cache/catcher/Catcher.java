package com.codebal.cache.catcher;

import com.codebal.cache.catcher.logger.CacheLogger;

import java.util.function.Function;
import java.util.function.Supplier;

public class Catcher {

    int waitCreateIntervalMilSec = 500; //캐시가 생성되기 기다리는 시간
    int waitCreateRetryMaxCnt = 10; //캐시가 생성을 기다리는 최대 재시도 수

    int defaultRefreshSec = 60;
    int defaultExpireSec = 60 * 5;

    boolean async = true;

    Function<CacheData, Boolean> cacheResourceSetter;
    Function<Object, CacheData> cacheResourceGetter;

    CacheMaker cacheMaker;

    ICatcherSignal iCatcherSignal = null;

    public Catcher(Function cacheResourceSetter, Function cacheResourceGetter){
        this.cacheResourceSetter = cacheResourceSetter;
        this.cacheResourceGetter = cacheResourceGetter;

        cacheMaker = new CacheMaker(this);
    }

    public Catcher(ICatcherSignal iCatcherSignal){
        this.iCatcherSignal = iCatcherSignal;

        cacheMaker = new CacheMaker(this);
    }

    public boolean setCacheData(CacheData cacheData){
        //CacheLogger.debug("--- 캐시입력 : " + cacheData + " ---");
        if(iCatcherSignal != null)
            return iCatcherSignal.cacheResourceSetter(cacheData);
        else
            return cacheResourceSetter.apply(cacheData);
    }

    public void setCacheData(Supplier<CacheData> supplier){
        CacheData cacheData;
        try{
            cacheData = supplier.get();
            setCacheData(cacheData);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public CacheData getCacheData(String key){
        try{
            if(iCatcherSignal != null){
                return iCatcherSignal.cacheResourceGetter(key);
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

    public <T> T get(String key){
        CacheData cacheData = getCacheData(key);
        if(cacheData == null)
            return null;
        else
            return cacheData.getData();
    }

    public CacheData set(String key, Supplier<Object> supplier, Integer refresh_sec, Integer expire_sec, Boolean asyncRefresh, Boolean startNotNull){
        CacheData cacheData = new CacheData(key, null, refresh_sec, expire_sec, asyncRefresh, startNotNull);
        return set(cacheData, supplier);
    }

    public CacheData set(CacheData cacheData, Supplier<Object> supplier){
        return cacheMaker.make(cacheData, supplier);
    }

    /**
     * asyncRefresh = true 일때는 모든 요청이 비동기이므로 startNotNull 값을 true 로 강제할 필요가 있음
     */
    public CacheData getSetCData(String key, Supplier<Object> supplier, Integer refresh_sec, Integer expire_sec, Boolean asyncRefresh, Boolean startNotNull){
        CacheData cacheData = getCacheData(key);

        boolean needCreate = false;
        boolean isNull = false;
        boolean needWait = false;
        if(cacheData != null){
            if(!cacheData.isCreating() && cacheData.needRefresh())
                needCreate = true;

            if(cacheData.needForceRefresh())
                needCreate = true;

            //캐시가 생성중인데 데이터는 없고 startNotNull이 false 일때
            if(cacheData.isCreating()){
                if(cacheData.getData() == null && cacheData.startNotNull){
                    needWait = true;
                }
                else if(!cacheData.asyncRefresh){
                    needWait = true;
                }
            }

            if(needWait){
                waitCreateCache(cacheData.key);
            }
        }
        else{
            needCreate = true;
            isNull = true;
            cacheData = new CacheData(key, null, refresh_sec, expire_sec, asyncRefresh, startNotNull);
        }

        if(needCreate){ //캐시 생성을 해야 한다
            boolean nowCreating = startCreatingCache(cacheData);

            if(nowCreating){
                boolean async = true;
                if(!isNull && !cacheData.asyncRefresh){ //캐시가 존재하고, 비동기 리프래시를 안한다면
                    async = false;
                }
                else if(isNull && cacheData.startNotNull){ //캐시가 존재하지 않고, 캐시를 생성한 후에 받는다면
                    async = false;
                }

                if(async){ //비동기 캐시 생성
                    set(cacheData, supplier);
                }
                else{ //동기 캐시 생성
                    cacheData.setData(supplier.get());
                    setCacheData(cacheData);
                    endCreatingCache(cacheData);
                }
            }
        }

        return cacheData;
    }

    public <T> T getSet(String key, Supplier<Object> supplier, Integer refresh_sec, Integer expire_sec, Boolean asyncRefresh, Boolean startNotNull){
        CacheData cacheData = getSetCData(key, supplier, refresh_sec, expire_sec, asyncRefresh, startNotNull);
        if(cacheData == null)
            return null;
        return (T) cacheData.getData();
    }


//    public <T> T getSet2(String key, int refresh_sec, int expire_sec, boolean wait_new, Supplier<T> supplier){
//        CacheData currentCacheData = getCacheData(key);
//        boolean waitOther = false;
//        boolean needCreate = false;
//        boolean async = !wait_new;
//        T resultData = null;
//
//        //--------------  캐시가 있음  ---------------
//        if(currentCacheData != null){
//            if(currentCacheData.getData() != null){ //캐시가 있고, 데이타가 있다면 기다릴필요없이 가져다 쓴다.
//                async = true;
//                resultData = (T)currentCacheData.getData();
//            }
//            else{
//                needCreate = true;
//
//                if(currentCacheData.isCreating() && !async){ // 캐시가 이미 생성중인 상태
//                    needCreate = false;
//                    waitOther = true;
//                }
//            }
//
//            if(!currentCacheData.isCreating() && currentCacheData.needRefresh()) { //캐시 만료시간이 지났다면 다시 만들어 갱신.
//                needCreate = true;
//            }
//
//            if(currentCacheData.isCreating() && currentCacheData.needForceRefresh()){
//                needCreate = true;
//            }
//        }
//        //--------------  캐시가 없음  ---------------
//        else{
//            needCreate = true;
//        }
//
//        //--------------  캐시 생성 요청  ---------------
//        if(needCreate){
//            //동기식
//            if(!async){
//                CacheLogger.debug("--- 캐시생성 동기: " + key + " ---");
//                startCreatingCache(key, refresh_sec, expire_sec);
//                currentCacheData = createCacheData(key, ()->{
//                    T data = supplier.get();
//                    CacheData cacheData = new CacheData(key, data, refresh_sec, expire_sec, null, null);
//                    setCacheData(cacheData);
//                    return cacheData;
//                });
//                resultData = currentCacheData.getData();
//            }
//            //비동기식
//            else{
//                boolean canStart = startCreatingCache(key, refresh_sec, expire_sec);
//                if(canStart){
//                    CacheLogger.debug("--- 캐시생성 비동기: " + key + " ---");
//                    this.setCacheData(()->{
//                        CacheData cacheData = createCacheData(key, ()->{
//                            T data = supplier.get();
//                            CacheData newCacheData = new CacheData(key, data, refresh_sec, expire_sec, null, null);
//                            return newCacheData;
//                        });
//                        return cacheData;
//                    });
//                }
//            }
//        }
//        //--------------  캐시 생성 요청없음  ---------------
//        else{
//            if(waitOther){
//                CacheData cd = waitCreateCache(key);
//                resultData = cd.getData();
//
//            }
//        }
//
//        return resultData;
//    }

    public boolean startCreatingCache(String key, int refresh_sec, int expire_sec){
        CacheData cacheData = getCacheData(key);
        if(cacheData == null)
            cacheData = new CacheData(key, null, refresh_sec, expire_sec, null, null);

        return startCreatingCache(cacheData);
    }

    public boolean startCreatingCache(CacheData cacheData){
        if(cacheData.isCreating())
            return false;

        cacheData.setCreating(true);
        setCacheData(cacheData);

        return true;
    }

    public boolean breakCreatingCache(CacheData cacheData){
        if(!cacheData.isCreating())
            return false;

        cacheData.setCreating(false);
        setCacheData(cacheData);

        return true;
    }

    public void endCreatingCache(String key){
        endCreatingCache(getCacheData(key));
    }

    public void endCreatingCache(CacheData cacheData){
        if(cacheData != null){
            CacheData newCacheData = new CacheData(cacheData.key, cacheData.getData(), cacheData.refresh_sec, cacheData.expire_sec, cacheData.asyncRefresh, cacheData.startNotNull);
            setCacheData(newCacheData);
        }
    }


    public int getCauchbaseDefaultExpiry(){
        int default_expiry = 60 * 60;
        return default_expiry;
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
        while(tryCnt < waitCreateRetryMaxCnt){
            tryCnt++;
            cacheData = getCacheData(key);
            if(cacheData != null && !cacheData.isCreating()){
                return cacheData;
            }
            CacheLogger.debug(this.getClass(), "wait for creating cache / key:" + key + " ---");

            if(tryCnt > waitCreateRetryMaxCnt){
                CacheLogger.error(this.getClass(), "cache waiting too long / count:" + tryCnt + ", key:" + key + " ---" );
                breakCreatingCache(cacheData);
                break;
            }

            try{
                Thread.sleep(waitCreateIntervalMilSec);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

        return null;
    }

    public void remove(String key){

    }

    public Function<CacheData, Boolean> getCacheResourceSetter() {
        return cacheResourceSetter;
    }

    public Function<Object, CacheData> getCacheResourceGetter() {
        return cacheResourceGetter;
    }
}
