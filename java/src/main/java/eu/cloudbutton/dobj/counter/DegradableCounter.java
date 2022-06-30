package eu.cloudbutton.dobj.counter;

import eu.cloudbutton.dobj.counter.AbstractCounter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class build a Counter on top of a Snapshot object.
 *
 * @author Boubacar Kane
 * */
public class DegradableCounter extends AbstractCounter {

    private final ConcurrentMap<Thread, BoxLong> count;
    private final ThreadLocal<BoxLong> local;

    /**
     * Creates a new Counter initialized with the initial value 0.
     */
    public DegradableCounter() {
        this.count = new ConcurrentHashMap<>();
        this.local = ThreadLocal.withInitial(() -> {
            BoxLong l = new BoxLong();
            count.put(Thread.currentThread(), l);
            return l;
        });
    }

    /**
     * Increments the current value.
     */
    public void increment() {
        local.get().setVal(local.get().getVal() + 1);
    }

    /**
     * Adds the given value to the current value of the Counter.
     * @param delta the value added to the Counter.
     * @throws IllegalArgumentException if the value is different than 1.
     */
    public void increment(int delta) throws IllegalArgumentException{
        if (delta != 1)
            throw new IllegalArgumentException("This counter only supports increments of 1");

        increment();
    }

    @Override
    public long incrementAndGet() {
        increment();
        return 0;
    }

    @Override
    public long addAndGet(int delta) {
        increment(delta);
        return 0;
    }

    /**
     * Returns the current value.
     * @return the current value stored by this object.
     */
    @Override
    public long read() {
        int total = 0;
        for (BoxLong v : count.values()) {
            total += v.getVal();
        }
        return total;
    }
}
