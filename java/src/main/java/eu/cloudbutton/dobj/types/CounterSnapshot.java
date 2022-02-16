package eu.cloudbutton.dobj.types;

import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class build a Counter on top of a Snapshot object.
 *
 * @author Boubacar Kane
 * */
public class CounterSnapshot extends AbstractCounter{

    private final Snapshot<Counter> snapobject;
    private final ThreadLocal<Triplet<Counter, AtomicInteger, List<Counter>>> tripletThreadLocal;
    private final ConcurrentMap<Thread, List<Counter>> embedded_snaps;

    /**
     * Creates a new Counter initialized with the initial value 0.
     */
    public CounterSnapshot() {
        snapobject = new Snapshot<>(new ConcurrentHashMap<>());
        tripletThreadLocal = ThreadLocal.withInitial(() -> {
            Triplet<Counter, AtomicInteger, List<Counter>> triplet = new Triplet<>(new Counter(), new AtomicInteger(), new ArrayList<>());
            snapobject.obj.put(Thread.currentThread(), triplet);
            return triplet;
        });
        embedded_snaps = new ConcurrentHashMap<>();
        embedded_snaps.put(Thread.currentThread(), new ArrayList<>());

    }

    /**
     * Increments the current value of the Counter.
     */
    @Override
    public void increment() {
        List<Counter> embedded_snap = snapobject.snap();
        tripletThreadLocal.get().getValue0().increment();
        tripletThreadLocal.get().getValue1().incrementAndGet();
        embedded_snaps.put(Thread.currentThread(), embedded_snap);
    }

    /**
     * Adds the given value to the current value of the Counter.
     * @param delta the value added to the Counter.
     */
    @Override
    public void increment(int delta) {
        List<Counter> embedded_snap = snapobject.snap();
        tripletThreadLocal.get().getValue0().increment(delta);
        tripletThreadLocal.get().getValue1().incrementAndGet();
        embedded_snaps.put(Thread.currentThread(), embedded_snap);
    }


    /**
     * Returns the current value of the Counter.
     * @return the current value stored by this object.
     */
    @Override
    public int read(){

        List<Counter> list = snapobject.snap();

        int result = 0;

        for (Counter count : list) {
            result += count.read();
        }
        return result;
    }
}
