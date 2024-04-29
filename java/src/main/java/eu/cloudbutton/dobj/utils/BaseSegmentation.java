package eu.cloudbutton.dobj.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BaseSegmentation<T> implements Segmentation<T> {

    public static Method currentCarrierThread;

    static {
        try {
            currentCarrierThread = Thread.class.getDeclaredMethod("currentCarrierThread");
            currentCarrierThread.setAccessible(true);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private Class<T> clazz;
    private final List<T> segments;
    private ThreadLocal<T> local;

    public BaseSegmentation(Class<T> clazz, int parallelism) {
        this.clazz = clazz;
        this.segments = new CopyOnWriteArrayList<>(); // FIXME loom requires a (skip?) linked list instead here, where the index is the carrier ID.
        this.local = new ThreadLocal<>();
    }

    @Override
    public final T segmentFor(Object x) {
        if (local.get() == null) {
            try {
                T segment = this.clazz.getDeclaredConstructor().newInstance();
                segments.add(segment);
                local.set(segment);
            } catch (Throwable e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
        }
        return local.get();
    }

    @Override
    public final List<T> segments() {
        return segments;
    }

    public static final int carrierID() {
        try {
            return (int) ((Thread) currentCarrierThread.invoke(null)).threadId();
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
