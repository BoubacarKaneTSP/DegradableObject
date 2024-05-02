package eu.cloudbutton.dobj.incrementonly;

import eu.cloudbutton.dobj.types.Counter;
import eu.cloudbutton.dobj.utils.BaseSegmentation;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * This class build a Counter on top of a Snapshot object.
 *
 * @author Boubacar Kane
 * */
public class CounterIncrementOnly extends BaseSegmentation<BoxedLong> implements Counter {
    
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
        super(BoxedLong.class);
    }

    /**
     * Increments the current value.
     * @return 0
     */
    @Override
    public long incrementAndGet() {
        segmentFor(null).val += 1;
        UNSAFE.storeFence();
        return 0;
    }

    public void increment(){
        segmentFor(null).val +=1 ;
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
        for (BoxedLong v : segments) {
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
        segmentFor(null).val -= delta;
        UNSAFE.storeFence();
        return 0;
    }

    @Override
    public long decrementAndGet() {
        segmentFor(null).val -= 1;
        UNSAFE.storeFence();
        return 0;
    }

    public void decrement(){
        segmentFor(null).val -=1 ;
        UNSAFE.storeFence();
    }
}
