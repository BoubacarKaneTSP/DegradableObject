package eu.cloudbutton.dobj.Benchmark.Tester;

import java.lang.reflect.InvocationTargetException;

public abstract class Filler<T> {

    protected final T object;
    protected final long nbOps;

    public Filler(T object, long nbOps) {
        this.object = object;
        this.nbOps = nbOps;
    }


    public abstract void fill() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException;
}
