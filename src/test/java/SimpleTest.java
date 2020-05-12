import com.codebal.cache.catcher.CacheData;
import com.codebal.cache.catcher.Catcher;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class SimpleTest {
    static Catcher catcher;
    static public int cacheCreateCount = 0;

    public static void main(String[] args) {

        log("Catcher Test");

        Map<String, Object> cacheResource = new HashMap<>();

        catcher = new Catcher(
                (cacheData)->{
                    try{
                        cacheResource.put(cacheData.key, cacheData);
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

        int count = 0;
        while(count < 3){
            Thread request = new Thread(()->{
                while(true){
                    try{
                        Thread.sleep((int)(Math.random()*1000) + 2000);
                        String threadName = Thread.currentThread().getName();
                        long st = System.currentTimeMillis();
                        String value = getSetCacheData("key1", threadName);
                        long delay = System.currentTimeMillis() - st;
                        log(threadName + " | " + value + " | (delay " + delay + ")");
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }, "thread-" + count);
            request.start();
            count++;
        }
    }

    static String getSetCacheData(String key, String id){
        return catcher.getSet(key, ()->{
            try{
                int delay = 1000;
                Thread.sleep(delay);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
                cacheCreateCount++;
                String rtn = id + " / cacheCreateCount " + cacheCreateCount + " / " + simpleDateFormat.format(System.currentTimeMillis());
                log("cache created [" + rtn + "] processing time :  " + delay);
                return rtn;
            }
            catch(Exception e){
                return "error";
            }
        }, 5, 100, true, false);
    }

    static void log(String msg){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        String nowTime = simpleDateFormat.format(System.currentTimeMillis());
        System.out.println(nowTime + " -- " + msg);
    }
}
