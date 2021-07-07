package eu.cloudbutton.dobj.types;

import org.javatuples.Pair;

public class CounterSnapshotSRMW extends AbstractCounter{

    private final SnapshotSRMW<Integer> snapobject;
    private final ThreadLocal<Integer> counterThreadLocal;

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

    public void increment(int val) {
        counterThreadLocal.set(counterThreadLocal.get()+val);
        snapobject.update(counterThreadLocal.get());
    }

    @Override
    public void increment() {
        counterThreadLocal.set(counterThreadLocal.get()+1);
        snapobject.update(counterThreadLocal.get());
    }

    @Override
    public int read() {
        int result = 0;

        for (Integer i : snapobject.snap()) {
            result += i;
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
