package eu.cloudbutton.dobj.types;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DegradableCounter extends AbstractCounter {

    private final ConcurrentMap<Thread, Integer> count;
    private final ThreadLocal<Integer> local;

    public DegradableCounter() {
        this.count = new ConcurrentHashMap<>();
        this.local = ThreadLocal.withInitial(() -> {
            Integer l = 0;
            count.put(Thread.currentThread(), l);
            return l;
        });
    }

    @Override
    public void increment() {
        local.set(local.get()+1);
    }

    public void increment(int val) {
        local.set(local.get()+val);
    }

    @Override
    public int read() {
        int total = 0;
        for (Integer v : count.values()) {
            total += v;
        }
        return total;
    }

    @Override
    public void write() {
        increment();
    }

    @Override
    public void write(int val) {
        increment(val);
    }
}
