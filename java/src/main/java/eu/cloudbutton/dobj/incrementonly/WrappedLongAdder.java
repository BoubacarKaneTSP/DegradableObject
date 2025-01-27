package eu.cloudbutton.dobj.incrementonly;

import eu.cloudbutton.dobj.types.Counter;

import java.util.concurrent.atomic.LongAdder;

public class WrappedLongAdder extends LongAdder implements Counter {

    private LongAdder counter;

    public WrappedLongAdder(){
        counter = new LongAdder();
    }

    @Override
    public long incrementAndGet() {
        counter.increment();
        return 0;
    }

    @Override
    public long addAndGet(int delta) {
        counter.add(delta);
        return 0;
    }

    @Override
    public long get() {

        return counter.longValue();
    }

    @Override
    public long decrementAndGet(int delta) {
        counter.add(-delta);
        return 0;
    }

    @Override
    public long decrementAndGet() {
        counter.decrement();
        return 0;
    }
}
