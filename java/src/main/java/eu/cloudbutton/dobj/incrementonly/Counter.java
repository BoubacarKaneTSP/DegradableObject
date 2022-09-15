package eu.cloudbutton.dobj.incrementonly;

public interface Counter {
    /**
     * Increments the current value of the Counter.
     *
     * @return the value of the counter after increment.
     */
    long incrementAndGet();

    /**
     * Adds the given value to the current value of the Counter.
     *
     * @param delta the value added to the Counter.
     * @return the value of the counter after adding delta.
     */
    long addAndGet(int delta);

    /**
     * Returns the current value of the Counter.
     *
     * @return the current value stored by this object.
     */
    long read();

    /**
     * Retrieve the given value to the current value of the Counter.
     *
     * @param delta the value retrieved to the Counter.
     * @return the value of the counter after retrieving delta.
     */
    long decrementAndGet(int delta);

    /**
     * Decrement the current value of the Counter.
     *
     * @return the value of the counter after decrementing.
     */
    long decrementAndGet();
}
