package codebal.catcher.test;

import java.util.function.Consumer;

public class ExThread extends Thread {
    String id;

    public ExThread(Consumer consumer, String id){
        super(()->{
            consumer.accept(id);
        }, id);
        this.id = id;
    }
}
