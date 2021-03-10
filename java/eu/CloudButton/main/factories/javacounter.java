package factories;

import java.util.concurrent.atomic.AtomicInteger;

public class javacounter extends counter{

    private final AtomicInteger count;

    public javacounter() { count = new AtomicInteger(); }

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
