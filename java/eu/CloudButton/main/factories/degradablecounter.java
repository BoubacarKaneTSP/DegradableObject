package factories;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class degradablecounter extends counter{

    private final ConcurrentMap<String, AtomicInteger> count;

    public degradablecounter() {
        this.count = new ConcurrentHashMap<>();
    }

    @Override
    public void increment() {
        String pid = Thread.currentThread().getName();
//        count.compute(pid, (key, val) -> val == null ? new AtomicInteger(): val.incrementAndGet() );
        AtomicInteger val = count.getOrDefault(pid, new AtomicInteger());
        val.incrementAndGet();
        count.put(pid, val);
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
