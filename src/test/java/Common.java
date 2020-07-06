import com.codebal.cache.catcher.CacheData;
import com.codebal.cache.catcher.CacheError;
import com.codebal.cache.catcher.Catcher;
import com.codebal.cache.catcher.ICatcherSignal;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Common {

    static public int cacheCreateCount = 0;
    static Map<String, Object> cacheResource = new HashMap<>();

    static Catcher getCatcherSimple(){

        Common.log("Catcher init");
        Catcher catcher = new Catcher(
                (cData)->{
                    try{
                        cacheResource.put(cData.key, cData);
                        return true;
                    }
                    catch(Exception e){
                        e.printStackTrace();
                        return false;
                    }
                },
                (cacheKey)->{
                    return (CacheData)cacheResource.get(cacheKey);
                }
        );
        catcher.setWaitCreateIntervalMs(100);
        catcher.setWaitCreateRetryMaxCnt(10);
        
        return catcher;
    }

    static Catcher getCacherWithSignal(){
        ICatcherSignal iCatcherSignal = new ICatcherSignal() {
            @Override
            public Boolean cacheResourceSetter(CacheData cacheData) {
                try{
                    cacheResource.put(cacheData.key, cacheData);
                    return true;
                }
                catch(Exception e){
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            public CacheData cacheResourceGetter(Object cacheKey) {
                return (CacheData)cacheResource.get(cacheKey);
            }

            @Override
            public CacheData cacheCreateCustomErrorHandler(CacheError cacheError) {
                log("에러 발생 했구만");
                return cacheError.getCacheData();
            }

            @Override
            public CacheData waitTimeoverHandler(CacheData cacheData) {
                return cacheData;
            }
        };

        Catcher catcher = new Catcher(iCatcherSignal);
        return catcher;
    }

    static String getSetTest(Catcher catcher, String key, String callThreadName, int delay, Supplier supplier, boolean asyncRefresh, boolean nonBlocking){
        //int refresh_sec = (int)(Math.random() * 3) + 3;
        int refresh_sec = 3;
        CacheData cacheData = catcher.getSetCacheData(key, ()->{
            try{
                Thread.sleep(delay);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
                cacheCreateCount++;
                String rtn = callThreadName + " / cacheCreateCount " + cacheCreateCount + " / " + simpleDateFormat.format(System.currentTimeMillis());
                if(supplier != null){
                    rtn = rtn + " / suppler " + supplier.get();
                }
                log("thread: " + callThreadName + " : cache created [" + rtn + "] cache create dealy :  " + delay);
                return rtn;
            }
            catch(InterruptedException e){
                e.printStackTrace();
                return "error";
            }
        }, refresh_sec, 100, asyncRefresh, nonBlocking);
        //log("refresh_sec:" + refresh_sec);
        //log(cacheData.toString());
        return cacheData.getData();
    }

    static void log(String msg){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        String nowTime = simpleDateFormat.format(System.currentTimeMillis());
        System.out.println(nowTime + " [" + Thread.currentThread().getName() + "] - " + msg);
    }
}
