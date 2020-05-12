# java-catcher

Catcher is Java Cache Manager (multi request, async refresh, ..)

<br/>
<br/> 

Catcher  
![Position of Catcher](https://raw.githubusercontent.com/codebal/java-catcher/master/etc/img/Position%20of%20Catcher.jpg)  

<br/>
<br/>

Extended Cache Data  
![Extended Cache Data](https://raw.githubusercontent.com/codebal/java-catcher/master/etc/img/Extended%20Cache%20Data.JPG)

<br/>
<br/>
<br/>

Single Request on Sync  
![Single Request on Sync](https://raw.githubusercontent.com/codebal/java-catcher/master/etc/img/Single%20Request%20on%20Sync.JPG)
***
Single Request with **Catcher**  
![Single Request with Catcher](https://raw.githubusercontent.com/codebal/java-catcher/master/etc/img/Single%20Request%20with%20Catcher.JPG)

<br/>
<br/>
<br/>

Multi Request on Sync  
![Multi Request on Sync](https://raw.githubusercontent.com/codebal/java-catcher/master/etc/img/Multi%20Request%20on%20Sync.JPG)
***
Multi Request with **Catcher**  
![Multi Request with Catcher](https://raw.githubusercontent.com/codebal/java-catcher/master/etc/img/Multi%20Request%20with%20Catcher.JPG)

<br/>
<br/>

#### **jmeter web response test**  
threads : 10000  
periods : 10 seconds  
elapsed time : 16-18 seconds  
ehcache cache setting :  
  - version = 2.10.6
  - timeToLiveSeconds = 3  
  - 1 sec delay (Thread.sleep)

catcher setting :  
 - refresh time = 3 seconds  
 - expire time = 5 seconds (also ehcache timeToLiveSeconds = 5)

<br/>

##### RESULT  
 - ehcache only :  
      cache creating occurred : **800**  
      response delay occurred : every cache creating  
 - eacache **+ catcher**  
      cache creating occurred : **6**  
      response delay occurred : only first cache creating 

<br/>

Response time using Ehcache 
![Response time using Ehcache](https://raw.githubusercontent.com/codebal/java-catcher/master/etc/img/ehcache-Response%20Time%20Graph.jpg)
***
Response time using Ehcache **+ Catcher** 
![Response time using Ehcache + Catcher](https://raw.githubusercontent.com/codebal/java-catcher/master/etc/img/catcher-Response%20Time%20Graph.jpg)      

<br/>
<br/>
<br/>

### Usage

```java
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
```

```java
// out

// startNotNull = true
// cache value is not returned until the cache is initialized
13:54:42.385 -- Catcher Test
13:54:45.820 -- cache created [thread-1 / cacheCreateCount 1 / 13:54:45.820] processing time :  1000
13:54:45.820 -- thread-1 | thread-1 / cacheCreateCount 1 / 13:54:45.820 | (delay 1036)
13:54:46.327 -- thread-0 | thread-1 / cacheCreateCount 1 / 13:54:45.820 | (delay 1126)
13:54:46.334 -- thread-2 | thread-1 / cacheCreateCount 1 / 13:54:45.820 | (delay 1302)
13:54:48.001 -- thread-1 | thread-1 / cacheCreateCount 1 / 13:54:45.820 | (delay 0)
13:54:48.897 -- thread-2 | thread-1 / cacheCreateCount 1 / 13:54:45.820 | (delay 0)
13:54:49.114 -- thread-0 | thread-1 / cacheCreateCount 1 / 13:54:45.820 | (delay 0)
13:54:50.365 -- thread-1 | thread-1 / cacheCreateCount 1 / 13:54:45.820 | (delay 40)
13:54:51.032 -- thread-2 | thread-1 / cacheCreateCount 1 / 13:54:45.820 | (delay 0)
13:54:51.365 -- cache created [thread-1 / cacheCreateCount 2 / 13:54:51.365] processing time :  1000
13:54:51.743 -- thread-0 | thread-1 / cacheCreateCount 2 / 13:54:51.365 | (delay 0)
13:54:52.416 -- thread-1 | thread-1 / cacheCreateCount 2 / 13:54:51.365 | (delay 0)
13:54:53.052 -- thread-2 | thread-1 / cacheCreateCount 2 / 13:54:51.365 | (delay 0)
13:54:54.251 -- thread-0 | thread-1 / cacheCreateCount 2 / 13:54:51.365 | (delay 0)
13:54:54.665 -- thread-1 | thread-1 / cacheCreateCount 2 / 13:54:51.365 | (delay 0)
13:54:55.791 -- thread-2 | thread-1 / cacheCreateCount 2 / 13:54:51.365 | (delay 0)
13:54:56.259 -- thread-0 | thread-1 / cacheCreateCount 2 / 13:54:51.365 | (delay 0)
13:54:56.791 -- cache created [thread-2 / cacheCreateCount 3 / 13:54:56.791] processing time :  1000
13:54:56.851 -- thread-1 | thread-2 / cacheCreateCount 3 / 13:54:56.791 | (delay 0)
13:54:58.592 -- thread-2 | thread-2 / cacheCreateCount 3 / 13:54:56.791 | (delay 1)
13:54:58.837 -- thread-0 | thread-2 / cacheCreateCount 3 / 13:54:56.791 | (delay 0)
13:54:59.798 -- thread-1 | thread-2 / cacheCreateCount 3 / 13:54:56.791 | (delay 0)
...

// startNotNull = false
// return cache value to null before the cache is initialized
10:18:15.885 -- Catcher Test
10:18:18.355 -- thread-2 | null | (delay 20)
10:18:18.377 -- thread-1 | null | (delay 0)
10:18:18.889 -- thread-0 | null | (delay 0)
10:18:19.355 -- cache created [thread-2 / cacheCreateCount 1 / 10:18:19.355] processing time :  1000
10:18:20.573 -- thread-1 | thread-2 / cacheCreateCount 1 / 10:18:19.355 | (delay 0)
10:18:21.157 -- thread-2 | thread-2 / cacheCreateCount 1 / 10:18:19.355 | (delay 0)
10:18:21.183 -- thread-0 | thread-2 / cacheCreateCount 1 / 10:18:19.355 | (delay 0)
10:18:22.639 -- thread-1 | thread-2 / cacheCreateCount 1 / 10:18:19.355 | (delay 0)
10:18:23.921 -- thread-2 | thread-2 / cacheCreateCount 1 / 10:18:19.355 | (delay 0)
10:18:23.953 -- thread-0 | thread-2 / cacheCreateCount 1 / 10:18:19.355 | (delay 0)
10:18:25.522 -- thread-1 | thread-2 / cacheCreateCount 1 / 10:18:19.355 | (delay 0)
10:18:25.999 -- thread-0 | thread-2 / cacheCreateCount 1 / 10:18:19.355 | (delay 0)
10:18:26.522 -- cache created [thread-1 / cacheCreateCount 2 / 10:18:26.522] processing time :  1000
10:18:26.905 -- thread-2 | thread-1 / cacheCreateCount 2 / 10:18:26.522 | (delay 0)
10:18:28.085 -- thread-0 | thread-1 / cacheCreateCount 2 / 10:18:26.522 | (delay 0)
10:18:28.514 -- thread-1 | thread-1 / cacheCreateCount 2 / 10:18:26.522 | (delay 0)
10:18:29.646 -- thread-2 | thread-1 / cacheCreateCount 2 / 10:18:26.522 | (delay 0)
10:18:30.187 -- thread-0 | thread-1 / cacheCreateCount 2 / 10:18:26.522 | (delay 0)
10:18:31.251 -- thread-1 | thread-1 / cacheCreateCount 2 / 10:18:26.522 | (delay 0)
...
```