package eu.cloudbutton.dobj.types;

import java.util.concurrent.atomic.AtomicInteger;

public class Counter extends AbstractCounter {

    private final AtomicInteger count;

    public Counter(Counter counter) { count = new AtomicInteger(counter.get()); }
    public Counter(int i) { count = new AtomicInteger(i); }
    public Counter() { count = new AtomicInteger(); }

    @Override
    public void increment() {
        count.incrementAndGet();
    }

    public void increment(int val) {
        count.addAndGet(val);
    }

    @Override
    public int read() {
        return count.intValue();
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
