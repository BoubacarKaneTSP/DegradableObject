package eu.cloudbutton.dobj.types;

public abstract class AbstractCounter {

    public abstract void increment();
    public abstract int read();
    public abstract void write();
    public abstract void write(int val);
}
