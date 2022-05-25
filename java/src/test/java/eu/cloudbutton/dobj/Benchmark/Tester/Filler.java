package eu.cloudbutton.dobj.Benchmark.Tester;

public abstract class Filler<T> {

    protected final T object;
    protected final long nbOps;

    public Filler(T object, long nbOps) {
        this.object = object;
        this.nbOps = nbOps;
    }


    public abstract void fill();
}
