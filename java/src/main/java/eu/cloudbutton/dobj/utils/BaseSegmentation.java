package eu.cloudbutton.dobj.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

    private final Map<Integer,Integer> redirect;

    public BaseSegmentation(Class<T> clazz, int parallelism) {
        this.clazz = clazz;
        this.segments = new CopyOnWriteArrayList<>();
        this.redirect = new ConcurrentHashMap<>();
    }

    @Override
    public final T segmentFor(Object x) {
        int index = (int) Thread.currentThread().threadId();
        // int index = carrierID();

        if (!redirect.containsKey(index)) {
            System.out.println("Index: " + index);
            try {
                T ret = this.clazz.getDeclaredConstructor().newInstance();
                this.segments.add(ret);
                Integer r = redirect.putIfAbsent(index, this.segments.indexOf(ret));
                assert r == null;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new Error(e);
            }
        }
        return segments.get(redirect.get(index));
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
