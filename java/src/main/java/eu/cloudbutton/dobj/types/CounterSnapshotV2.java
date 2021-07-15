package eu.cloudbutton.dobj.types;

import org.javatuples.Pair;

public class CounterSnapshotV2 extends AbstractCounter{

    private final SnapshotSRMW<Counter> snapobject;
    private final ThreadLocal<Counter> counterThreadLocal;

    public CounterSnapshotV2(){
        snapobject = new SnapshotSRMW<>();
        counterThreadLocal = ThreadLocal.withInitial(() -> {
            Counter counter = new Counter();
            snapobject.memory.put(Thread.currentThread(), new Pair<>( new Pair<>(new Counter(), 0),
                    new Pair<>(new Counter(), 0)
            ));
            return counter;
        });

    }

    public void increment(int val) {
        counterThreadLocal.get().increment(val);
        snapobject.update(counterThreadLocal.get());
    }

    @Override
    public void increment() {
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
