package eu.cloudbutton.dobj.register;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class SWMRLong {

    private long l;

    private static final long valueOffset;
    private static final sun.misc.Unsafe UNSAFE;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);
            valueOffset = UNSAFE.objectFieldOffset(SWMRLong.class.getDeclaredField("l"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public SWMRLong(){
        set(0);
    }

    public SWMRLong(long l){
        set(l);
    }

    public long get() {
        return UNSAFE.getLongVolatile(this,valueOffset);
    }

    public long lazyGet(){
        return this.l;
    }

    public void set(long v) {
        UNSAFE.putLongVolatile(this,valueOffset,v);
    }

    public void increment(long v) {
        set(this.l+v);
    }

}
