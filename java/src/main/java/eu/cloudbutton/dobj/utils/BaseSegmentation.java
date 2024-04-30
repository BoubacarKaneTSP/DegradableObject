package eu.cloudbutton.dobj.utils;


import jdk.internal.vm.annotation.ForceInline;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BaseSegmentation<T> implements Segmentation<T> {

    private Class<T> clazz;
    private final Map<Integer,T> segments;
    private ThreadLocal<T> local;

    public BaseSegmentation(Class<T> clazz, int parallelism) {
        this.clazz = clazz;
        this.segments = new ConcurrentHashMap<>(0);
        this.local = new ThreadLocal<>();
    }

    @Override
    public T segmentFor(Object x) {
        if (local.get() == null) {
            int carrier = Carrier.carrierID();
            try {
                T segment = this.clazz.getDeclaredConstructor().newInstance();
                local.set(segments.putIfAbsent(carrier, segment) == null ? segment : segments.get(carrier));
            } catch (Throwable e) {
                throw new RuntimeException();
            }
        }
        return local.get();
    }

    @Override
    @ForceInline
    public final Collection<T> segments() {
        return segments.values();
    }

}
