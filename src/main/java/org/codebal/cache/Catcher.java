package org.codebal.cache;

import org.codebal.cache.logger.CcLogger;

import java.util.function.Function;
import java.util.function.Supplier;

public class Catcher {

    int waitCreateIntervalMilSec = 500; //캐시가 생성되기 기다리는 시간
    int waitCreateRetryMaxCnt = 10; //캐시가 생성을 기다리는 최대 재시도 수

    int defaultRefreshSec = 60;
    int defaultExpireSec = 60 * 5;

    boolean async = true;

    Function<CcData, Boolean> cacheResourceSetter;
    Function<Object, CcData> cacheResourceGetter;

    CcMaker ccMaker;

    ICatcherSignal iCatcherSignal = null;

    public Catcher(Function cacheResourceSetter, Function cacheResourceGetter){
        this.cacheResourceSetter = cacheResourceSetter;
        this.cacheResourceGetter = cacheResourceGetter;

        ccMaker = new CcMaker(this);
    }

    public Catcher(ICatcherSignal iCatcherSignal){
        this.iCatcherSignal = iCatcherSignal;

        ccMaker = new CcMaker(this);
    }

    public boolean setCacheData(CcData ccData){
        //CcLogger.debug("--- 캐시입력 : " + ccData + " ---");
        if(iCatcherSignal != null)
            return iCatcherSignal.cacheResourceSetter(ccData);
        else
            return cacheResourceSetter.apply(ccData);
    }

    public void setCacheData(Supplier<CcData> supplier){
        CcData ccData;
        try{
            ccData = supplier.get();
            setCacheData(ccData);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public CcData getCacheData(String key){
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
//        key = CcData.getLimitCacheKey(key);
//        try{
//            SerializableDocument couchdata = bucket.get(key, SerializableDocument.class);
//            if(couchdata == null || couchdata.content() == null){
//                //logger.debug("--- 캐시없음 : " + key + " ---");
//                return null;
//            }
//            else{
//                try{
//                    CcData ccData = (CcData)couchdata.content();
//                    //logger.debug("--- 캐시있음 : " + key + " ---");
//                    return ccData;
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

//    public CcData getSetCacheData(CcData ccData, Supplier<CcData> supplier){
//        CcData currentCacheData = this.getCacheData(ccData.key);
//
//        boolean needCreate = false;
//        if(ccData != null){
//            if(!ccData.isCreating() && ccData.needRefresh())
//                needCreate = true;
//        }
//        else{
//            needCreate = true;
//        }
//
//        if(needCreate){
//            if(ccData.asyncRefresh){
//                currentCacheData = createCacheData(ccData.key, ()->{
//                    CcData newCacheData = supplier.get();
//                    setCacheData(ccData);
//                    return ccData;
//                });
//            }
//            else{
//                createCacheData(ccData.key, ()->{
//                    this.setCacheData(supplier);
//                    return null;
//                });
//            }
//        }
//        else{
//            if(wait && currentCacheData.isCreating())
//                waitCreateCache(ccData.key);
//        }
//
//        return currentCacheData;
//    }

    public <T> T get(String key){
        CcData ccData = getCacheData(key);
        if(ccData == null)
            return null;
        else
            return ccData.getData();
    }

    public CcData set(String key, Supplier<Object> supplier, Integer refresh_sec, Integer expire_sec, Boolean asyncRefresh, Boolean startNotNull){
        CcData ccData = new CcData(key, null, refresh_sec, expire_sec, asyncRefresh, startNotNull);
        return set(ccData, supplier);
    }

    public CcData set(CcData ccData, Supplier<Object> supplier){
        return ccMaker.make(ccData, supplier);
    }

    /**
     * asyncRefresh = true 일때는 모든 요청이 비동기이므로 startNotNull 값을 true 로 강제할 필요가 있음
     */
    public CcData getSetCData(String key, Supplier<Object> supplier, Integer refresh_sec, Integer expire_sec, Boolean asyncRefresh, Boolean startNotNull){
        CcData ccData = getCacheData(key);

        boolean needCreate = false;
        boolean isNull = false;
        boolean needWait = false;
        if(ccData != null){
            if(!ccData.isCreating() && ccData.needRefresh())
                needCreate = true;

            if(ccData.needForceRefresh())
                needCreate = true;

            //캐시가 생성중인데 데이터는 없고 startNotNull이 false 일때
            if(ccData.isCreating()){
                if(ccData.getData() == null && ccData.startNotNull){
                    needWait = true;
                }
                else if(!ccData.asyncRefresh){
                    needWait = true;
                }
            }

            if(needWait){
                waitCreateCache(ccData.key);
            }
        }
        else{
            needCreate = true;
            isNull = true;
            ccData = new CcData(key, null, refresh_sec, expire_sec, asyncRefresh, startNotNull);
        }

        if(needCreate){ //캐시 생성을 해야 한다
            boolean nowCreating = startCreatingCache(ccData);

            if(nowCreating){
                boolean async = true;
                if(!isNull && !ccData.asyncRefresh){ //캐시가 존재하고, 비동기 리프래시를 안한다면
                    async = false;
                }
                else if(isNull && ccData.startNotNull){ //캐시가 존재하지 않고, 캐시를 생성한 후에 받는다면
                    async = false;
                }

                if(async){ //비동기 캐시 생성
                    set(ccData, supplier);
                }
                else{ //동기 캐시 생성
                    ccData.setData(supplier.get());
                    setCacheData(ccData);
                    endCreatingCache(ccData);
                }
            }
        }

        return ccData;
    }

    public <T> T getSet(String key, Supplier<Object> supplier, Integer refresh_sec, Integer expire_sec, Boolean asyncRefresh, Boolean startNotNull){
        CcData ccData = getSetCData(key, supplier, refresh_sec, expire_sec, asyncRefresh, startNotNull);
        if(ccData == null)
            return null;
        return (T)ccData.getData();
    }


//    public <T> T getSet2(String key, int refresh_sec, int expire_sec, boolean wait_new, Supplier<T> supplier){
//        CcData currentCacheData = getCacheData(key);
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
//                CcLogger.debug("--- 캐시생성 동기: " + key + " ---");
//                startCreatingCache(key, refresh_sec, expire_sec);
//                currentCacheData = createCacheData(key, ()->{
//                    T data = supplier.get();
//                    CcData ccData = new CcData(key, data, refresh_sec, expire_sec, null, null);
//                    setCacheData(ccData);
//                    return ccData;
//                });
//                resultData = currentCacheData.getData();
//            }
//            //비동기식
//            else{
//                boolean canStart = startCreatingCache(key, refresh_sec, expire_sec);
//                if(canStart){
//                    CcLogger.debug("--- 캐시생성 비동기: " + key + " ---");
//                    this.setCacheData(()->{
//                        CcData ccData = createCacheData(key, ()->{
//                            T data = supplier.get();
//                            CcData newCacheData = new CcData(key, data, refresh_sec, expire_sec, null, null);
//                            return newCacheData;
//                        });
//                        return ccData;
//                    });
//                }
//            }
//        }
//        //--------------  캐시 생성 요청없음  ---------------
//        else{
//            if(waitOther){
//                CcData cd = waitCreateCache(key);
//                resultData = cd.getData();
//
//            }
//        }
//
//        return resultData;
//    }

    public boolean startCreatingCache(String key, int refresh_sec, int expire_sec){
        CcData ccData = getCacheData(key);
        if(ccData == null)
            ccData = new CcData(key, null, refresh_sec, expire_sec, null, null);

        return startCreatingCache(ccData);
    }

    public boolean startCreatingCache(CcData ccData){
        if(ccData.isCreating())
            return false;

        ccData.setCreating(true);
        setCacheData(ccData);

        return true;
    }

    public boolean breakCreatingCache(CcData ccData){
        if(!ccData.isCreating())
            return false;

        ccData.setCreating(false);
        setCacheData(ccData);

        return true;
    }

    public void endCreatingCache(String key){
        endCreatingCache(getCacheData(key));
    }

    public void endCreatingCache(CcData ccData){
        if(ccData != null){
            CcData newCcData = new CcData(ccData.key, ccData.getData(), ccData.refresh_sec, ccData.expire_sec, ccData.asyncRefresh, ccData.startNotNull);
            setCacheData(newCcData);
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
            CcLogger.error(this.getClass(), "error createCacheData / key:" + key);
            e.printStackTrace();
        }
        finally {
            //logger.debug("-------- end create cache / key : " + key + "--------");
        }

        return result;
    }

    public CcData waitCreateCache(String key) {

        CcData ccData;
        int tryCnt = 0;
        while(tryCnt < waitCreateRetryMaxCnt){
            tryCnt++;
            ccData = getCacheData(key);
            if(ccData != null && !ccData.isCreating()){
                return ccData;
            }
            CcLogger.debug(this.getClass(), "wait for creating cache / key:" + key + " ---");

            if(tryCnt > waitCreateRetryMaxCnt){
                CcLogger.error(this.getClass(), "cache waiting too long / count:" + tryCnt + ", key:" + key + " ---" );
                breakCreatingCache(ccData);
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

    public Function<CcData, Boolean> getCacheResourceSetter() {
        return cacheResourceSetter;
    }

    public Function<Object, CcData> getCacheResourceGetter() {
        return cacheResourceGetter;
    }
}
