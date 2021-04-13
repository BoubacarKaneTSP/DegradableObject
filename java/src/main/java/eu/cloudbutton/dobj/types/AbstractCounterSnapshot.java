package eu.cloudbutton.dobj.types;

import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractCounterSnapshot<T> extends Snapshot<T>{
    public AbstractCounterSnapshot() {
        super(new ConcurrentHashMap<>());
    }

    protected abstract void write();
    protected abstract int read();
}
