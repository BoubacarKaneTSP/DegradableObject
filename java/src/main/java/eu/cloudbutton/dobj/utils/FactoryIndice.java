package eu.cloudbutton.dobj.utils;

import eu.cloudbutton.dobj.incrementonly.BoxedLong;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FactoryIndice {

    private final Map<Integer,Integer> indices;
    private Integer next;
    private final ThreadLocal<Integer> local;
    @Getter
    private final int parallelism;

    public FactoryIndice(int parallelism){
        this.next = 0;
        this.indices = new HashMap<>();
        this.local = new ThreadLocal();
        this.parallelism = parallelism;
    }

    public final Integer getIndice(){
        if(local.get() == null) {
            int carrier = Carrier.carrierID();
            synchronized (this) {
                if (!indices.containsKey(carrier)) {
                    indices.put(carrier, next);
                }
                next++;
            }
            local.set(indices.get(carrier));
        }
        return local.get();
    }
}
