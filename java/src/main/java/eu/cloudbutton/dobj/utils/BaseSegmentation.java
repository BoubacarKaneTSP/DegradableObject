package eu.cloudbutton.dobj.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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

    private final ConcurrentHashMap<Integer,T> segments;

    public BaseSegmentation(Class<T> clazz, int parallelism) {
        this.clazz = clazz;
        this.segments = new ConcurrentHashMap<>();
    }

    @Override
    public final T segmentFor(Object x) {
        // int index = (int) Thread.currentThread().threadId();
        try {
            int index = carrierID();
            if (!segments.containsKey(index)) {
                T segment = this.clazz.getDeclaredConstructor().newInstance();
                T previous = segments.putIfAbsent(index, segment);
                assert previous == null;
            }
            return segments.get(index);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public final Collection<T> segments() {
        return segments.values();
    }

    public static final int carrierID() {
        try {
            return (int) ((Thread) currentCarrierThread.invoke(null)).threadId();
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
