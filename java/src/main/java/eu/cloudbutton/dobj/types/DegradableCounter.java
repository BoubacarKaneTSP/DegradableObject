package eu.cloudbutton.dobj.types;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class build a Counter on top of a Snapshot object.
 *
 * @author Boubacar Kane
 * */
public class DegradableCounter extends AbstractCounter {

    private final ConcurrentMap<Thread, AtomicInteger> count;
    private final ThreadLocal<AtomicInteger> local;

    /**
     * Creates a new Counter initialized with the initial value 0.
     */
    public DegradableCounter() {
        this.count = new ConcurrentHashMap<>();
        this.local = ThreadLocal.withInitial(() -> {
            AtomicInteger l = new AtomicInteger(0);
            count.put(Thread.currentThread(), l);
            return l;
        });
    }

    /**
     * Increments the current value.
     */
    @Override
    public void increment() {
        local.get().incrementAndGet();
    }

    /**
     * Adds the given value to the current value of the Counter.
     * @param delta the value added to the Counter.
     * @throws IllegalArgumentException if the value is different than 1.
     */
    @Override
    public void increment(int delta) throws IllegalArgumentException{
        if (delta != 1)
            throw new IllegalArgumentException("This counter only supports increments of 1");

        local.get().addAndGet(delta);
    }

    /**
     * Returns the current value.
     * @return the current value stored by this object.
     */
    @Override
    public int read() {
        int total = 0;
        for (AtomicInteger v : count.values()) {
            total += v.get();
        }
        return total;
    }
}
