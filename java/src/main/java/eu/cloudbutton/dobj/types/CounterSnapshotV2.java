package eu.cloudbutton.dobj.types;

import org.javatuples.Pair;

public class CounterSnapshotV2 extends AbstractCounter{

    private final SnapshotV2<Counter> snapobject;
    private final ThreadLocal<Counter> counterThreadLocal;

    public CounterSnapshotV2(){
        snapobject = new SnapshotV2<>();
        counterThreadLocal = new ThreadLocal<>();
    }

    public void increment(int val) {
        int name = Integer.parseInt(Thread.currentThread().getName().substring(5).replace("-thread-",""));
        if(!snapobject.memory.containsKey(name)){
            counterThreadLocal.set(new Counter());
            snapobject.memory.put   (   name,
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
        int name = Integer.parseInt(Thread.currentThread().getName().substring(5).replace("-thread-",""));
        if(!snapobject.memory.containsKey(name)){
            counterThreadLocal.set(new Counter());
            snapobject.memory.put   (   name,
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
