package eu.cloudbutton.dobj.utils;

import eu.cloudbutton.dobj.incrementonly.BoxedLong;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ExtendedSegmentation<T> implements Segmentation<T>{

    private final List<T> segments;
    private final FactoryIndice factoryIndice;

    public ExtendedSegmentation(Class<T> clazz, FactoryIndice factoryIndice) {
        this.factoryIndice = factoryIndice;
        int parallelism = factoryIndice.getParallelism();
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
                System.out.println(Thread.currentThread().getName() + " failed to initialize indice "+ indice + " for obj : " + x);
                indice = obj.getReference().get();
                System.out.println(Thread.currentThread().getName() + " have now indice : " + indice + " for obj : " + x);

                System.out.println(Thread.currentThread().getName() + " is verifiying if the indice it gets have not change");

                assert indice == obj.getReference().get() : indice + " : " + obj.getReference().get();
            }else{
                System.out.println(Thread.currentThread().getName() + " successfully initialized indice  for obj : " + x);
                assert System.identityHashCode(indice) == System.identityHashCode(obj.getReference().get()) : "failed to insert in atomic reference";
            }
        }

        assert indice != null : "Indice is null for " + Thread.currentThread().getName();

        System.out.println(Thread.currentThread().getName() + " => " + System.identityHashCode(indice) + " : " + indice);

        System.out.println("====================");

        System.out.println("There is " + segments.size() + " segments for " + Thread.currentThread().getName() + " in the list of segments : " + System.identityHashCode(segments));


        for (int i = 0; i < segments.size(); i++) {

            System.out.println(Thread.currentThread().getName() + " have segment : " + System.identityHashCode(segments.get(i)) + " in " + i + " position");
        }

        System.out.println("====================");

        segment = segments().get((int) indice.getVal());

        System.out.println(Thread.currentThread().getName() + " => " + indice.getVal() + " : " + System.identityHashCode(segment));


        assert segment != null : "Value not associated with a segment";

        System.out.println("Segment for " + x + " : " + System.identityHashCode(segment));
        return segment;
    }

    @Override
    public List<T> segments() {
        return segments;
    }
}
