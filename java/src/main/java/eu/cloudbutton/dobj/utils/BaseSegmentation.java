package eu.cloudbutton.dobj.utils;


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
                segments.putIfAbsent(carrier, segment); // avoiding a possible race
                local.set(segments.get(carrier));
            } catch (Throwable e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
        }
        return local.get();
    }

    @Override
    public final Collection<T> segments() {
        return segments.values();
    }

}
