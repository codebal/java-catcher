import codebal.catcher.CcData;
import codebal.catcher.CcLogger;
import codebal.catcher.Catcher;
import codebal.catcher.test.ExThread;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class Main {

    static Catcher catcher;
    static public int cacheCount = 0;

    public static void main(String[] args) throws Exception {

        System.out.println("Catcher Test");

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
        while(count < 5){
            ExThread request = new ExThread((id)->{
                while(true){
                    try{
                        Thread.sleep((int)(Math.random()*1000) + 1000);
                        //Thread.sleep(1000);
                        String value = getSetCacheData("key1");
                        CcLogger.debug(Main.class, id + " - 캐시조회 : " + value);
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
    static String getSetCacheData(String key){
        return catcher.getSet(key, ()->{
            try{
                //Thread.sleep(  (int)(Math.random()*1000*10));
                int delay = 3000;
                Thread.sleep(delay);
                CcLogger.debug(Main.class, "캐시생성됨 delay :  " + delay + "");
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
                String rtn = cacheCount + " - " + simpleDateFormat.format(System.currentTimeMillis());
                cacheCount++;
                return rtn;
            }
            catch(Exception e){
                return "error";
            }
        }, 5, 100, true, true);
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
