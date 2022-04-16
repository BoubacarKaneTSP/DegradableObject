package eu.cloudbutton.dobj.types;

import java.util.concurrent.atomic.AtomicLong;

/**
 * This class encapsulate the AtomicCounter from Java.
 *
 * @author Boubacar Kane
 * */
public class Counter extends AbstractCounter {

    private final AtomicLong count;

    /**
     * Creates a new Counter initialized with the value stored in the specified counter.
     * @param counter The counter whose value is given to the new.
     */
    public Counter(Counter counter) { count = new AtomicLong(counter.read()); }

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
     */
    @Override
    public void increment() {
        count.incrementAndGet();
    }

    /**
     * Atomically adds the given value to the current value of the Counter.
     * @param delta the value added to the Counter.
     */
    @Override
    public void increment(int delta) {
        count.addAndGet(delta);
    }

    /**
     * Returns the current value of the Counter.
     * @return the current value stored by this object.
     */
    @Override
    public long read() {
        return count.longValue();
    }

}
