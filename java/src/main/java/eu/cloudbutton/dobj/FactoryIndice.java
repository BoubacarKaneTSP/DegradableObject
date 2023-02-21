package eu.cloudbutton.dobj;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

public class FactoryIndice {
    @Getter
    protected static ThreadLocal<Integer> local = null;
    private final AtomicInteger next;
    @Getter
    private final Integer parallelism;

    public FactoryIndice(int parallelism){
        this.parallelism = parallelism;
        this.next = new AtomicInteger();
        local =  ThreadLocal.withInitial(() -> -1);
    }

    public int getIndice(){

        if (local.get() == -1){
            Integer indice = next.getAndIncrement();
            local.set(indice);
            assert local.get() == indice : "Failed to update local";
            assert indice < parallelism : "The indice generated ("+indice+") excess the number of segments ("+parallelism+")";
        }

        return local.get();
    }
}
