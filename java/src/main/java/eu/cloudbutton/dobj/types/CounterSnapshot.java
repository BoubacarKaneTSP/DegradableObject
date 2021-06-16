package eu.cloudbutton.dobj.types;

import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CounterSnapshot extends AbstractCounter{

    private final Snapshot<Counter> snapobject;
    private final ThreadLocal<Triplet<Counter, AtomicInteger, ArrayList<Counter>>> tripletThreadLocal;

    public CounterSnapshot() {
        snapobject = new Snapshot<>(new ConcurrentHashMap<>());
        tripletThreadLocal = new ThreadLocal<>();
    }

    @Override
    public void increment() {
        int name = Integer.parseInt(Thread.currentThread().getName().substring(5).replace("-thread-",""));

        if (!snapobject.obj.containsKey(name)){
            tripletThreadLocal.set(new Triplet<>(new Counter(), new AtomicInteger(), new ArrayList<>()));
            snapobject.obj.put(name, new Triplet<>(new Counter(), new AtomicInteger(), new ArrayList<>()));
        }

        List<Counter> embedded_snap = snapobject.snap();
        tripletThreadLocal.get().getValue0().write();
        tripletThreadLocal.get().getValue1().incrementAndGet();

        snapobject.obj.put(name, new Triplet<>( tripletThreadLocal.get().getValue0(),
                tripletThreadLocal.get().getValue1(),
                embedded_snap));
    }

    public void increment(int val) {
        int name = Integer.parseInt(Thread.currentThread().getName().substring(5).replace("-thread-",""));

        if (!snapobject.obj.containsKey(name)){
            tripletThreadLocal.set(new Triplet<>(new Counter(), new AtomicInteger(), new ArrayList<>()));
            snapobject.obj.put(name, new Triplet<>(new Counter(), new AtomicInteger(), new ArrayList<>()));
        }

        List<Counter> embedded_snap = snapobject.snap();
        tripletThreadLocal.get().getValue0().write(val);
        tripletThreadLocal.get().getValue1().incrementAndGet();

        snapobject.obj.put(name, new Triplet<>( tripletThreadLocal.get().getValue0(),
                tripletThreadLocal.get().getValue1(),
                embedded_snap));
    }

    @Override
    public void write(){ increment(); }

    @Override
    public void write(int val) { increment(val); }

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
