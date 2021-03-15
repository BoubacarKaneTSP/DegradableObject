package eu.cloudbutton.dobj.types;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DegradableCounter extends Counter {

    private final ConcurrentMap<String, AtomicInteger> count;

    private final ThreadLocal<AtomicInteger> local;

    public DegradableCounter() {
        this.count = new ConcurrentHashMap<>();
        this.local = new ThreadLocal<>();
    }

    @Override
    public void increment() {
        String pid = Thread.currentThread().getName();
        if (!count.containsKey(pid)) {
            local.set(new AtomicInteger());
            this.count.put(pid,local.get());
        }
        local.get().incrementAndGet();
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
}
