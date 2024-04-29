package eu.cloudbutton.dobj.utils;

import eu.cloudbutton.dobj.incrementonly.BoxedLong;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FactoryIndice {

    private final Map<Integer,BoxedLong> indices;
    private final AtomicInteger next;
    private final ThreadLocal<BoxedLong> local;
    @Getter
    private final int parallelism;

    public FactoryIndice(int parallelism){
        this.next = new AtomicInteger();
        this.indices = new ConcurrentHashMap<>();
        this.local = new ThreadLocal();
        this.parallelism = parallelism;
    }

    public final BoxedLong getIndice(){
        if(local.get() == null) {
            int carrier = Carrier.carrierID();
            indices.putIfAbsent(carrier, new BoxedLong(next.getAndIncrement()));
            local.set(indices.get(carrier));
        }
        return local.get();
    }
}
