package eu.cloudbutton.dobj.utils;

import jdk.internal.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.util.TreeMap;

public class ConsistentHashSegmentation<T> implements Segmentation<T> {

    private static final int parallelism = Runtime.getRuntime().availableProcessors();

    private TreeMap<Integer, T> segments = new TreeMap<>();

    public ConsistentHashSegmentation(Class<T> clazz) {
        TreeMap<Integer,T> treeMap = new TreeMap<>();
        try {
            Constructor<T> constructor = clazz.getConstructor();
            for(int i=0; i<parallelism; i++) {
                int hash = (Helpers.getExecutorNamePrefix() + i).hashCode();
                treeMap.put(hash, constructor.newInstance());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        U.fullFence();
        U.putReferenceRelease(this,SEGMENTS,treeMap);
    }

    @Override
    public T segmentFor(Object x) {
        Integer key = segments.ceilingKey(x.hashCode());
        key = key == null ? key : segments.firstKey();
        return segments.get(key);
    }

    // Unsafe mechanic

    private static final Unsafe U = Unsafe.getUnsafe();
    private static final long SEGMENTS = U.objectFieldOffset(ConsistentHashSegmentation.class, "segments");

}
