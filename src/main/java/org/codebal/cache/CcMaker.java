package org.codebal.cache;

import java.util.List;
import java.util.function.Supplier;

public class CcMaker {

    Catcher catcher;

    List<CcThread> cacheRunnerList;
    int threadIndex = 0;

    public CcMaker(Catcher catcher){
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
                        crThread.getCcRunner().isTooLong();
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
        return "ccRunner" + threadIndex++;
    }

    public CcData make(CcData ccData, Supplier<Object> supplier){
        CcRunner ccRunner = new CcRunner(catcher, ccData, supplier);

        CcThread thread = new CcThread(ccRunner, getThreadName());
        thread.start();

        return ccData;
    }

    public void addCacheRunner(CcThread ccThread){
        cacheRunnerList.add(ccThread);
    }

    public void removeCacheRunner(String thread_name){
        CcThread removeCcThread = null;
//        for(CcThread crThread : cacheRunnerList){
//
//        }
        cacheRunnerList.stream().forEach((crThread)->{
            if(crThread.getName().equals(thread_name)){
                cacheRunnerList.remove(crThread);
            }
        });
    }
}
