package eu.cloudbutton.dobj.types;

import org.javatuples.Pair;

public class CounterSnapshotSRMW extends AbstractCounter{

    private final SnapshotSRMW<Counter> snapobject;
    private final ThreadLocal<Counter> counterThreadLocal;
    private final ThreadLocal<Integer> name;

    public CounterSnapshotSRMW(){
        snapobject = new SnapshotSRMW<>();
        counterThreadLocal = new ThreadLocal<>();
        name = new ThreadLocal<>();
    }

    public void increment(int val) {
        if (name.get() == null)
            name.set(Integer.parseInt(Thread.currentThread().getName().substring(5).replace("-thread-","")));
        if(!snapobject.memory.containsKey(name.get())){
            counterThreadLocal.set(new Counter());
            snapobject.memory.put   (   name.get(),
                    new Pair<>( new Pair<>(new Counter(), 0),
                            new Pair<>(new Counter(), 0)
                    )
            );
        }
        counterThreadLocal.get().increment(val);
        snapobject.update(counterThreadLocal.get());
    }

    @Override
    public void increment() {
        if (name.get() == null)
            name.set(Integer.parseInt(Thread.currentThread().getName().substring(5).replace("-thread-","")));
        if(!snapobject.memory.containsKey(name.get())){
            counterThreadLocal.set(new Counter());
            snapobject.memory.put   (   name.get(),
                    new Pair<>( new Pair<>(new Counter(), 0),
                            new Pair<>(new Counter(), 0)
                    )
            );
        }
        counterThreadLocal.get().increment();
        snapobject.update(counterThreadLocal.get());
    }

    @Override
    public int read() {
        int result = 0;

        for (Counter counter: snapobject.snap()) {
            result += counter.read();
        }

        return result;
    }

    @Override
    public void write() {
        increment();
    }

    @Override
    public void write(int val) {
        increment(val);
    }
}
