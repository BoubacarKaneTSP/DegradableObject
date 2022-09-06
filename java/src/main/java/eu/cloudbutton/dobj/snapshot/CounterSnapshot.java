package eu.cloudbutton.dobj.snapshot;

import eu.cloudbutton.dobj.counter.Counter;
import eu.cloudbutton.dobj.counter.CounterJUC;
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
public class CounterSnapshot implements Counter {

    private final Snapshot<CounterJUC> snapobject;
    private final ThreadLocal<Triplet<CounterJUC, AtomicInteger, List<CounterJUC>>> tripletThreadLocal;
    private final ConcurrentMap<Thread, List<CounterJUC>> embedded_snaps;

    /**
     * Creates a new Counter initialized with the initial value 0.
     */
    public CounterSnapshot() {
        snapobject = new Snapshot<>(new ConcurrentHashMap<>());
        tripletThreadLocal = ThreadLocal.withInitial(() -> {
            Triplet<CounterJUC, AtomicInteger, List<CounterJUC>> triplet = new Triplet<>(new CounterJUC(), new AtomicInteger(), new ArrayList<>());
            snapobject.obj.put(Thread.currentThread(), triplet);
            return triplet;
        });
        embedded_snaps = new ConcurrentHashMap<>();
        embedded_snaps.put(Thread.currentThread(), new ArrayList<>());

    }

    /**
     * Increments the current value of the Counter.
     * @return
     */
    @Override
    public long incrementAndGet() {
        List<CounterJUC> embedded_snap = snapobject.snap();
        tripletThreadLocal.get().getValue0().incrementAndGet();
        tripletThreadLocal.get().getValue1().incrementAndGet();
        embedded_snaps.put(Thread.currentThread(), embedded_snap);

        return 0;
    }

    /**
     * Adds the given value to the current value of the Counter.
     * @param delta the value added to the Counter.
     * @return
     */
    @Override
    public long addAndGet(int delta) {
        List<CounterJUC> embedded_snap = snapobject.snap();
        tripletThreadLocal.get().getValue0().addAndGet(delta);
        tripletThreadLocal.get().getValue1().incrementAndGet();
        embedded_snaps.put(Thread.currentThread(), embedded_snap);

        return 0;
    }


    /**
     * Returns the current value of the Counter.
     * @return the current value stored by this object.
     */
    @Override
    public long read(){

        List<CounterJUC> list = snapobject.snap();

        int result = 0;

        for (CounterJUC count : list) {
            result += count.read();
        }
        return result;
    }
}
