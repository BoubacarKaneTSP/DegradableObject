package eu.cloudbutton.dobj.types;

public abstract class AbstractCounter {

    /**
     * Increments the current value of the Counter.
     */
    public abstract void increment();

    /**
     * Adds the given value to the current value of the Counter.
     * @param delta the value added to the Counter.
     */
    public abstract void increment(int delta);

    /**
     * Returns the current value of the Counter.
     * @return the current value stored by this object.
     */
    public abstract int read();
}
