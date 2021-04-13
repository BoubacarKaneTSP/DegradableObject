package eu.cloudbutton.dobj.types;

import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DegradableCounterSnapshot extends AbstractCounterSnapshot<DegradableCounter> {
    public DegradableCounterSnapshot() {
        super();
    }
    @Override
    public void write(){
        String name = Thread.currentThread().getName();

        if (!obj.containsKey(name)){
            obj.put(name, new Triplet<>(new DegradableCounter(), new AtomicInteger(), new ArrayList<>()));
        }
        List<DegradableCounter> embedded_snap = snap();
        obj.get(name).getValue0().write();
        obj.get(name).getValue1().incrementAndGet();
        obj.get(name).getValue2().clear(); // may break the snapshot algorithm if a process do not replace an embedded_snap
        obj.get(name).getValue2().addAll(embedded_snap);
    }

    @Override
    public int read() {
        List<DegradableCounter> list = snap();

        int result = 0;

        for (DegradableCounter count : list) {
            result += count.read();
        }
        return result;
    }
}
