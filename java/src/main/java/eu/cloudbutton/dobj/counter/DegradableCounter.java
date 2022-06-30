package eu.cloudbutton.dobj.counter;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class build a Counter on top of a Snapshot object.
 *
 * @author Boubacar Kane
 * */
public class DegradableCounter extends AbstractCounter {

    private final CopyOnWriteArrayList<AtomicLong> count;
    private final ThreadLocal<AtomicLong> local;

/*    private static final sun.misc.Unsafe UNSAFE;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new Error(e);
        }
    }*/

    /**
     * Creates a new Counter initialized with the initial value 0.
     */
    public DegradableCounter() {
        this.count = new CopyOnWriteArrayList<>();
        this.local = ThreadLocal.withInitial(() -> {
            AtomicLong l = new AtomicLong();
            count.add(l);
            return l;
        });
    }

    /**
     * Increments the current value.
     * @return 0
     */
    @Override
    public long incrementAndGet() {
        local.get().incrementAndGet();
//        local.get().setVal(local.get().getVal() + 1);
//        UNSAFE.storeFence();
        return 0;
    }
    /**
     * Adds the given value to the current value of the Counter.
     * @param delta the value added to the Counter.
     * @throws IllegalArgumentException if the value is different than 1.
     * @return 0
     */
    @Override
    public long addAndGet(int delta) throws IllegalArgumentException{
        if (delta != 1)
            throw new IllegalArgumentException("This counter only supports increments of 1");
        return incrementAndGet();
    }

    /**
     * Returns the current value.
     * @return the current value stored by this object.
     */
    @Override
    public long read() {
        long total = 0;
//        UNSAFE.loadFence();
        for (AtomicLong v : count) {
            total += v.get();
        }
        return total;
    }

}
