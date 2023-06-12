package eu.cloudbutton.dobj.utils;

import eu.cloudbutton.dobj.incrementonly.BoxedLong;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

public class FactoryIndice {
    @Getter
    protected static ThreadLocal<BoxedLong> local = null;
    private final AtomicInteger next;
    @Getter
    private final Integer parallelism;

    public FactoryIndice(int parallelism){
        this.parallelism = parallelism;
        this.next = new AtomicInteger();
        local =  ThreadLocal.withInitial(() -> new BoxedLong(-1));
        assert local.get().getVal() == -1 : "val => " + local.get().getVal() ;
    }

    public BoxedLong getIndice(){

        if (local.get().getVal() == -1 ){
            int indice = next.getAndIncrement();
            assert indice < parallelism : "The indice generated ("+indice+") excess the number of segments ("+parallelism+")";
//            System.out.println(Thread.currentThread().getName() + " => local ("+ System.identityHashCode(local.get().getVal())+") : " + local.get().getVal() + " | indice : " + indice);
            local.get().setVal(indice);
//            System.out.println(Thread.currentThread().getName() + " have indice : " + indice+ " => " + local.get().getVal());
            assert local.get().getVal() == indice : "Failed to update local";
        }

//        System.out.println(Thread.currentThread().getName() + " have indice : " + local.get().getVal());

        assert local.get().getVal() != -1 : "boxedlong not updated";

        return local.get();
    }
}
