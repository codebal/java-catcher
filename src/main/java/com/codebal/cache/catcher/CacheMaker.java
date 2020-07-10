package com.codebal.cache.catcher;

import java.util.List;
import java.util.function.Supplier;

public class CacheMaker {

    Catcher catcher;

    List<CacheThread> cacheRunnerList;
    long threadIndex = 0;

    public CacheMaker(Catcher catcher){
        this.catcher = catcher;

        //cacheRunnerList = new ArrayList<>();

        //startMonitor();
    }

    void startMonitor(){
        Thread thread = new Thread(()->{
            while(true){
                //System.out.println("쓰레드 모니터링 중");
                try{
                    cacheRunnerList.forEach((crThread)->{
                        crThread.getCacheRunner().isTooLong();
                    });
                    Thread.sleep(1000);
                }
                catch(Exception e){

                }
            }
        });
        thread.start();
    }

    String getThreadName(){
        return "CacheThread-" + threadIndex++;
    }

    public CacheData make(CacheData cacheData, Supplier<Object> supplier){
        CacheRunner cacheRunner = new CacheRunner(catcher, cacheData, supplier);

        CacheThread thread = new CacheThread(cacheRunner, getThreadName());
        thread.start();

        return cacheData;
    }

    public void addCacheRunner(CacheThread cacheThread){
        cacheRunnerList.add(cacheThread);
    }

    public void removeCacheRunner(String thread_name){
//        CacheThread removeCacheThread = null;
//        for(CacheThread crThread : cacheRunnerList){
//
//        }
        cacheRunnerList.stream().forEach((crThread)->{
            if(crThread.getName().equals(thread_name)){
                cacheRunnerList.remove(crThread);
            }
        });
    }
}
