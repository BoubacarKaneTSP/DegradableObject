package eu.cloudbutton.dobj.types;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DegradableCounter extends AbstractCounter {

    private final ConcurrentMap<Integer, AtomicInteger> count;
    private final ThreadLocal<AtomicInteger> local;
    private final ThreadLocal<Integer> name;

    public DegradableCounter() {
        this.count = new ConcurrentHashMap<>();
        this.local = new ThreadLocal<>();
        name = new ThreadLocal<>();
    }

    @Override
    public void increment() {
        if (name.get() == null) {
            name.set(Integer.parseInt(Thread.currentThread().getName().substring(5).replace("-thread-", "")));
        }
        if (!count.containsKey(name.get())) {
            local.set(new AtomicInteger());
            this.count.put(name.get(),local.get());
        }
        local.get().incrementAndGet();
    }

    public void increment(int val) {
        if (name.get() == null)
            name.set(Integer.parseInt(Thread.currentThread().getName().substring(5).replace("-thread-","")));

        if (!count.containsKey(name.get())) {
            local.set(new AtomicInteger());
            this.count.put(name.get(),local.get());
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
