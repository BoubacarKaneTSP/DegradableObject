package eu.cloudbutton.dobj.types;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DegradableCounter extends AbstractCounter {

    private final ConcurrentMap<Thread, AtomicInteger> count;
    private final ThreadLocal<AtomicInteger> local;

    public DegradableCounter() {
        this.count = new ConcurrentHashMap<>();
        this.local = ThreadLocal.withInitial(() -> {
            AtomicInteger l = new AtomicInteger();
            count.put(Thread.currentThread(), l);
            return l;
        });
    }

    @Override
    public void increment() {
        local.get().incrementAndGet();
    }

    public void increment(int val) {
        local.get().addAndGet(val);
    }

    @Override
    public int read() {
        int total = 0;
        for (AtomicInteger v : count.values()) {
            total += v.get();
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
