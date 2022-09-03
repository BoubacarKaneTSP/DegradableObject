package eu.cloudbutton.dobj.benchmark.tester;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

public abstract class Filler<T> {

    protected final T object;
    protected final long nbOps;

    public Filler(T object, long nbOps) {
        this.object = object;
        this.nbOps = nbOps;
    }


    public abstract void fill() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, ExecutionException, InterruptedException;
}
