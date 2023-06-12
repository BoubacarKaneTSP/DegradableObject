package eu.cloudbutton.dobj.utils;

import eu.cloudbutton.dobj.incrementonly.BoxedLong;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ExtendedSegmentation<T> implements Segmentation<T>{

    private final List<T> segments;
    private final FactoryIndice factoryIndice;

    public ExtendedSegmentation(Class<T> clazz, FactoryIndice factoryIndice) {
        this.factoryIndice = factoryIndice;
        int parallelism = factoryIndice.getParallelism()   ;
        this.segments = new ArrayList<>(parallelism);

        try {
            for (int i = 0; i < parallelism; i++) {
                this.segments.add(i, clazz.getDeclaredConstructor().newInstance());
                assert segments.get(i) != null : "Class not added to segment";
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        assert segments.size() == parallelism : "Wrong number of segments";
    }

    @Override
    public final T segmentFor(Object x) {

        SegmentAware obj = (SegmentAware) x;
        T segment;
        BoxedLong indice = obj.getReference().get();

        System.out.println(Thread.currentThread().getName() + " is adding with indice : " + indice + " for obj : " + x);

        if (indice == null){
            indice = factoryIndice.getIndice();

            if (!obj.getReference().set(indice)){
                System.out.println(Thread.currentThread().getName() + " failed to initialize indice");
                indice = obj.getReference().get();
                System.out.println(Thread.currentThread().getName() + " have now indice : " + indice);

                assert indice != obj.getReference().get() : indice + " : " + obj.getReference().get();
            }else{
                System.out.println(Thread.currentThread().getName() + " successfully initialized indice");
                assert System.identityHashCode(indice) == System.identityHashCode(obj.getReference().get()) : "failed to insert in atomic reference";
            }
        }

        assert indice != null : "Indice is null for " + Thread.currentThread().getName();

//        System.out.println(Thread.currentThread().getName() + " => " + System.identityHashCode(indice) + " : " + indice);

        segment = segments().get((int) indice.getVal());

//        System.out.println(Thread.currentThread().getName() + " => " + indice + " : " + ((Set)segment));

        assert segment != null : "Value not associated with a segment";

        return segment;
    }

    @Override
    public List<T> segments() {
        return segments;
    }
}
