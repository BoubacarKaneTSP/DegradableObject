package eu.cloudbutton.dobj.types;


import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractListSnapshot<T> extends Snapshot<T>{
    public AbstractListSnapshot() {
        super(new ConcurrentHashMap<>());
    }

    protected abstract void write(String val);
    protected abstract List read();
}
