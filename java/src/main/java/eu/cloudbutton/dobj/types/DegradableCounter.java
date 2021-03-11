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
        AtomicInteger total = new AtomicInteger();
        count.forEach((k,v) -> {
            total.addAndGet(v.intValue());
        });
        return total.intValue();
    }

    @Override
    public void write() {
        increment();
    }
}
