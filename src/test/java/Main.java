import com.codebal.cache.catcher.Catcher;

public class Main {

    static Catcher catcher1, catcher2;

    public static void main(String[] args) {

        Common.log("Catcher Test Start");

        catcher1 = Common.getCatcherSimple();

        catcher2 = Common.getCacherWithSignal();

        multiThreadTest(catcher1);
        //timeoutTest();
        //errorTest(catcher2);
        //forceRefreshTest();
    }

    static int getCount = 0;

    static public void multiThreadTest(Catcher catcher){

        int count = 0;
        while(count < 3){
            Thread request = new Thread(()->{
                while(true){
                    try{
                        Thread.sleep((int)(Math.random()*1000) + 2000);
                        //Thread.sleep(2000);
                        long st = System.currentTimeMillis();
                        String value = Common.getSetTest(catcher,"key1", Thread.currentThread().getName(), 1300,
                                ()->{
                                    getCount++;
                                    return getCount;
                                    //return 10 / (2 - getCount);
                                }, true, false);
                        long delay = System.currentTimeMillis() - st;
                        Common.log("get cache : " + value + " (delay " + delay + ")");
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

    static public void timeoutTest(Catcher catcher){
        int count = 0;
        while(count < 2){
            Thread request = new Thread(()->{
                while(true){
                    try{
                        Thread.sleep((int)(Math.random()*1000) + 2000);
                        long st = System.currentTimeMillis();
                        String value = Common.getSetTest(catcher,"key1", Thread.currentThread().getName(), 10000,
                                ()->{
                                    getCount++;
                                    return getCount;
                                    //return 10 / (2 - getCount);
                                }, true, true);
                        long delay = System.currentTimeMillis() - st;
                        Common.log("get cache : " + value + " (delay " + delay + ")");
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

    static public void errorTest(Catcher catcher){
        int count = 0;
        while(count < 3){
            Thread request = new Thread(()->{
                while(true){
                    try{
                        Thread.sleep((int)(Math.random()*1000) + 2000);
                        long st = System.currentTimeMillis();
                        String value = Common.getSetTest(catcher,"key1", Thread.currentThread().getName(), 2000,
                                ()->{
                                    int i = (int)(Math.random() * 2);
                                    int j = 1/i;
                                    return j;
                                }, true, false);
                        long delay = System.currentTimeMillis() - st;
                        Common.log("get cache : " + value + " (delay " + delay + ")");
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

    static public void forceRefreshTest(Catcher catcher){
        int count = 0;

        Thread request1 = new Thread(()->{
            long st = System.currentTimeMillis();
            String value = Common.getSetTest(catcher,"key1", Thread.currentThread().getName(), 1000 * 20,
                    ()->{
                        getCount++;
                        return getCount;
                        //return 10 / (2 - getCount);
                    }, true, true);
            long delay = System.currentTimeMillis() - st;
            Common.log("get cache : " + value + " (delay " + delay + ")");
        }, "thread-1");
        request1.start();

        try{
            Thread.sleep(1000 * 9);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        Thread request2 = new Thread(()->{
            long st = System.currentTimeMillis();
            String value = Common.getSetTest(catcher,"key1", Thread.currentThread().getName(), 1,
                    ()->{
                        getCount++;
                        return getCount;
                        //return 10 / (2 - getCount);
                    }, true, true);
            long delay = System.currentTimeMillis() - st;
            Common.log("get cache : " + value + " (delay " + delay + ")");
        }, "thread-2");
        request2.start();

        try{
            Thread.sleep(100);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        Thread request3 = new Thread(()->{
            long st = System.currentTimeMillis();
            String value = Common.getSetTest(catcher,"key1", Thread.currentThread().getName(), 1,
                    ()->{
                        getCount++;
                        return getCount;
                        //return 10 / (2 - getCount);
                    }, true, true);
            long delay = System.currentTimeMillis() - st;
            Common.log("get cache : " + value + " (delay " + delay + ")");
        }, "thread-3");
        request3.start();

//        while(count < 2){
//            Thread request = new Thread(()->{
//                while(true){
//                    try{
//                        //Thread.sleep((int)(Math.random()*1000) + 2000);
//                        long st = System.currentTimeMillis();
//                        String value = Common.getSetTest(catcher,"key1", Thread.currentThread().getName(), 100000,
//                                ()->{
//                                    getCount++;
//                                    return getCount;
//                                    //return 10 / (2 - getCount);
//                                }, true, true);
//                        long delay = System.currentTimeMillis() - st;
//                        Common.log("get cache : " + value + " (delay " + delay + ")");
//                    }
//                    catch (Exception e){
//                        e.printStackTrace();
//                    }
//
//                }
//            }, "thread-" + count);
//            request.start();
//            count++;
//        }
    }

}
