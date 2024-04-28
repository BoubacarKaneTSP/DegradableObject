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
        this.segments = new CopyOnWriteArrayList<>(); // FIXME loom requires a (skip?) linked list instead here.
        local = ThreadLocal.withInitial(() -> {
            try {
                T segment = this.clazz.getDeclaredConstructor().newInstance();
                segments.add(segment);
                return segment;
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    @Override
    public final T segmentFor(Object x) {
        return local.get();
    }

    @Override
    public final Collection<T> segments() {
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
