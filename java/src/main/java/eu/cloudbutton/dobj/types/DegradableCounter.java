package eu.cloudbutton.dobj.types;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DegradableCounter extends AbstractCounter {

    private final ConcurrentMap<Integer, AtomicInteger> count;

    private final ThreadLocal<AtomicInteger> local;

    public DegradableCounter() {
        this.count = new ConcurrentHashMap<>();
        this.local = new ThreadLocal<>();
    }

    @Override
    public void increment() {

        int pid = Integer.parseInt(Thread.currentThread().getName().substring(5).replace("-thread-",""));
        if (!count.containsKey(pid)) {
            local.set(new AtomicInteger());
            this.count.put(pid,local.get());
        }
        local.get().incrementAndGet();
    }

    public void increment(int val) {
        int pid = Integer.parseInt(Thread.currentThread().getName().substring(5).replace("-thread-",""));
        if (!count.containsKey(pid)) {
            local.set(new AtomicInteger());
            this.count.put(pid,local.get());
        }
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
