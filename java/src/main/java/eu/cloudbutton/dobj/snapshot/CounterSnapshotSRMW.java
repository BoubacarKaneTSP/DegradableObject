package eu.cloudbutton.dobj.snapshot;

import eu.cloudbutton.dobj.counter.AbstractCounter;
import org.javatuples.Pair;

/**
 * This class build a single reader multiple writer Counter on top of a Snapshot object.
 *
 * @author Boubacar Kane
 * */
public class CounterSnapshotSRMW extends AbstractCounter {

    private final SnapshotSRMW<Integer> snapobject;
    private final ThreadLocal<Integer> counterThreadLocal;

    /**
     * Creates a new Counter initialized with the initial value 0.
     */
    public CounterSnapshotSRMW(){
        snapobject = new SnapshotSRMW<>();
        counterThreadLocal = ThreadLocal.withInitial(
                () -> {
                    snapobject.memory.put(
                            Thread.currentThread(),
                            new Pair<>(new Pair<>(0, 0),new Pair<>(0, 0))
                    );
                    return 0;
                }
        );
    }

    /**
     * Adds the given value to the current value.
     * @param delta the value added to the Counter.
     * @return
     */
    @Override
    public long addAndGet(int delta) {
        counterThreadLocal.set(counterThreadLocal.get()+delta);
        snapobject.update(counterThreadLocal.get());
        return 0;
    }

    /**
     * Increments the current value of the Counter.
     * @return
     */
    @Override
    public long incrementAndGet() {
        counterThreadLocal.set(counterThreadLocal.get()+1);
        snapobject.update(counterThreadLocal.get());
        return 0;
    }

    /**
     * Returns the current value of the Counter.
     * @return the current value stored by this object.
     */
    @Override
    public long read() {
        int result = 0;

        for (Integer i : snapobject.snap()) {
            result += i;
        }

        return result;
    }


}
