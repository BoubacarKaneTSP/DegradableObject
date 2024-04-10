package eu.cloudbutton.dobj.incrementonly;

import jdk.internal.vm.annotation.Contended;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class build a Counter on top of a Snapshot object.
 *
 * @author Boubacar Kane
 * */
public class CounterIncrementOnly implements Counter {

    private final List<BoxedLong> count;
    protected final ThreadLocal<BoxedLong> local;

    protected static final sun.misc.Unsafe UNSAFE;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    /**
     * Creates a new Counter initialized with the initial value 0.
     */
    public CounterIncrementOnly() {
        this.count = new CopyOnWriteArrayList<>();
        this.local = ThreadLocal.withInitial(() -> {
            BoxedLong l = new BoxedLong();
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
        local.get().val += 1;

        UNSAFE.storeFence();
        return 0;
    }

    public void increment(){
        local.get().val +=1 ;
        UNSAFE.storeFence();
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
        UNSAFE.loadFence();
        for (BoxedLong v : count) {
            total += v.val;
        }
        return total;
    }

    public int intValue() {
        long ret = read();
        if (ret>=Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int)ret;
    }

    @Override
    public long decrementAndGet(int delta) {
        local.get().val -= delta;
        UNSAFE.storeFence();
        return 0;
    }

    @Override
    public long decrementAndGet() {
        local.get().val -= 1;
        UNSAFE.storeFence();
        return 0;
    }

    public void decrement(){
        local.get().val -=1 ;
        UNSAFE.storeFence();
    }
}
