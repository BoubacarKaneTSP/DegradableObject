package eu.cloudbutton.dobj.types;

import java.util.concurrent.ConcurrentHashMap;
public abstract class AbstractSetSnapshot<T> extends Snapshot<T>{
    public AbstractSetSnapshot() {
        super(new ConcurrentHashMap<>());
    }

    protected abstract void write(String val);
    protected abstract Set read();
}
