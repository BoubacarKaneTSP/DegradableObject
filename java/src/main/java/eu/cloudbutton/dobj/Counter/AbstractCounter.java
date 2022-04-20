package eu.cloudbutton.dobj.Counter;

public abstract class AbstractCounter {

    /**
     * Increments the current value of the Counter.
     * @return the value of the counter after increment.
     */
    public abstract long incrementAndGet();

    /**
     * Adds the given value to the current value of the Counter.
     * @param delta the value added to the Counter.
     * @return the value of the counter after adding delta.
     */
    public abstract long addAndGet(int delta);

    /**
     * Returns the current value of the Counter.
     * @return the current value stored by this object.
     */
    public abstract long read();
}
