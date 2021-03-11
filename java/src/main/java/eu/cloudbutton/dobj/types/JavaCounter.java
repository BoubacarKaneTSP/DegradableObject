package eu.cloudbutton.dobj.types;

import java.util.concurrent.atomic.AtomicInteger;

public class JavaCounter extends Counter {

    private final AtomicInteger count;

    public JavaCounter() { count = new AtomicInteger(); }

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
