package eu.cloudbutton.dobj.types;

import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DegradableSetSnapshot extends AbstractSetSnapshot<DegradableSet>{

    public DegradableSetSnapshot() {
        super();
    }

    @Override
    protected void write(String val) {
        String name = Thread.currentThread().getName();

        if (!obj.containsKey(name)){
            obj.put(name, new Triplet<>(new DegradableSet(), new AtomicInteger(), new ArrayList<>()));
        }
        List<DegradableSet> embedded_snap = snap();
        obj.get(name).getValue0().add(val);
        obj.get(name).getValue1().incrementAndGet();
        obj.get(name).getValue2().clear(); // may break the snapshot algorithm if a process do not replace an embedded_snap
        obj.get(name).getValue2().addAll(embedded_snap);
    }

    @Override
    protected Set read() {
        List<DegradableSet> list = snap();

        Set result = new Set();

        for (DegradableSet ens : list) {
                result.add(ens);
        }
        return result;
    }
}
