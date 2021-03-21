package eu.cloudbutton.dobj.types;

import java.util.concurrent.atomic.AtomicInteger;

public class Counter extends AbstractCounter {

    private final AtomicInteger count;

    public Counter() { count = new AtomicInteger(); }

    @Override
    public void increment() {
        count.incrementAndGet();
    }

    @Override
    public int read() {
        return count.intValue();
    }

    @Override
    public void write() {
        increment();
    }
}
