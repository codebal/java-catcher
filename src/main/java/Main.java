import org.codebal.cache.Catcher;
import org.codebal.cache.CcData;
import org.codebal.cache.test.ExThread;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class Main {

    static Catcher catcher;
    static public int cacheCreateCount = 0;

    public static void main(String[] args) throws Exception {

        log("Catcher Test");


        Map<String, Object> cacheResource = new HashMap<>();

        catcher = new Catcher(
                (cData)->{
                    CcData ccData = (CcData)cData;
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

        int count = 0;
        while(count < 3){
            ExThread request = new ExThread((id)->{
                while(true){
                    try{
                        Thread.sleep((int)(Math.random()*1000) + 2000);
                        //Thread.sleep(1000);
                        long st = System.currentTimeMillis();
                        String value = getSetCacheData("key1", id.toString());
                        long delay = System.currentTimeMillis() - st;
                        //CcLogger.debug(Main.class, id + " - get cache : " + value);
                        log(id + " | " + value + " | (delay " + delay + ")");
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }, "thread-" + count);
            request.start();
            count++;
        }




//
//        while(true){
//            String var1 = catcher.getSet("key1", ()->{
//                try{
//                    //Thread.sleep(  (int)(Math.random()*1000*10));
//                    Thread.sleep(  3000);
//                    System.out.println("캐시 생성하는데 3초 걸림");
//                    return "value1";
//                }
//                catch(Exception e){
//                    return "error";
//                }
//            }, 10, 100, true, false);
//            System.out.println("while var1 : " + var1);
//
//            Thread.sleep(1000);
//        }

    }
    static String getSetCacheData(String key, String id){
        return catcher.getSet(key, ()->{
            try{
                //Thread.sleep(  (int)(Math.random()*1000*10));
                int delay = 1000;
                Thread.sleep(delay);
                //CcLogger.debug(Main.class, "cache created / delay :  " + delay + "");
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

//    static String getSetCacheData2(String key, Object value, int dealy_sec){
//        return catcher.getSet(key, ()->{
//            try{
//                //Thread.sleep(  (int)(Math.random()*1000*10));
//                Thread.sleep(  1000 * dealy_sec);
//                CcLogger.debug("cache created after delay :  " + dealy_sec + "");
//                return value;
//            }
//            catch(Exception e){
//                return "error";
//            }
//        }, 5, 100, true, false);
//    }

}
