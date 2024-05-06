package eu.cloudbutton.dobj.utils;

import jdk.internal.misc.Unsafe;

import java.lang.reflect.Constructor;

public class HashSegmentation<T> implements Segmentation<T>{

    private static final int parallelism = Runtime.getRuntime().availableProcessors();

    private T[] segments;

    public HashSegmentation(Class<T> clazz) {
        T[] tab = (T[]) new Object[parallelism];
        try {
            Constructor<T> constructor = clazz.getConstructor();
            for (int i = 0; i < parallelism; i++) {
                tab[i] = constructor.newInstance();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        U.fullFence();
        U.putReferenceRelease(this,SEGMENTS,tab);
    }

    @Override
    public T segmentFor(Object x) {
        return segments[Math.abs(x.hashCode()%parallelism)];
    }

    // Unsafe mechanic

    private static final Unsafe U = Unsafe.getUnsafe();
    private static final long SEGMENTS = U.objectFieldOffset(ConsistentHashSegmentation.class, "segments");

}
