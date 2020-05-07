import com.codebal.cache.catcher.CacheData;
import com.codebal.cache.catcher.Catcher;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Common {

    static public int cacheCreateCount = 0;
    static Map<String, Object> cacheResource = new HashMap<>();

    static Catcher getCatcher(){

        Common.log("Catcher init");
        Catcher catcher = new Catcher(
                (cData)->{
                    CacheData ccData = (CacheData)cData;
                    try{
                        cacheResource.put(ccData.key, ccData);
                        return true;
                    }
                    catch(Exception e){
                        e.printStackTrace();
                        return false;
                    }
                },
                (cacheKey)->{
                    return cacheResource.get(cacheKey);
                }
        );
        return catcher;
    }

    static String getSetTest(Catcher catcher, String key, String callThreadName, int delay, Supplier supplier, boolean asyncRefresh, boolean startNotNull){
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
        }, 5, 100, asyncRefresh, startNotNull);
        log(cacheData.toString());
        return cacheData.getData();
    }

    static void log(String msg){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        String nowTime = simpleDateFormat.format(System.currentTimeMillis());
        System.out.println(nowTime + " [" + Thread.currentThread().getName() + "] - " + msg);
    }
}
