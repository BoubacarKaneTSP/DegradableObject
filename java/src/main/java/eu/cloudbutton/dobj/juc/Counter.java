package eu.cloudbutton.dobj.juc;

import java.util.concurrent.atomic.AtomicLong;

/**
 * This class encapsulate the AtomicCounter from Java.
 *
 * @author Boubacar Kane
 * */
public class Counter implements eu.cloudbutton.dobj.types.Counter {

    private final AtomicLong count;

    /**
     * Creates a new Counter initialized with the value stored in the specified counter.
     * @param counter The counter whose value is given to the new.
     */
    public Counter(Counter counter) { count = new AtomicLong(counter.get()); }

    /**
     * Creates a new Counter initialized with the given initial value.
     * @param initialValue the initial value.
     */
    public Counter(int initialValue) { count = new AtomicLong(initialValue); }

    /**
     * Creates a new Counter initialized with the initial value 0.
     */
    public Counter() { count = new AtomicLong(); }

    /**
     * Atomically increments the current value of the Counter.
     * @return the value of the counter after increment.
     */
    @Override
    public long incrementAndGet() {
        return count.incrementAndGet();
    }

    /**
     * Atomically adds the given value to the current value of the Counter.
     * @param delta the value added to the Counter.
     * @return the value of the counter after adding delta.
     */
    @Override
    public long addAndGet(int delta) {
         return count.addAndGet(delta);
    }

    /**
     * Returns the current value of the Counter.
     * @return the current value stored by this object.
     */
    @Override
    public long get() {
        return count.longValue();
    }

    @Override
    public long decrementAndGet(int delta) {
        return 0;
    }

    @Override
    public long decrementAndGet() {
        return count.decrementAndGet();
    }

}
