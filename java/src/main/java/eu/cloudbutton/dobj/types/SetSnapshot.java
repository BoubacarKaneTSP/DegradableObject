package eu.cloudbutton.dobj.types;

import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SetSnapshot<T> extends AbstractSetSnapshot<Set>{
    public SetSnapshot() {
        super();
    }

    @Override
    public void write(String val) {
        String name = Thread.currentThread().getName();

        if (!obj.containsKey(name)){
            obj.put(name, new Triplet<>(new Set<T>(), new AtomicInteger(), new ArrayList<>()));
        }
        List<Set> embedded_snap = snap();
        obj.get(name).getValue0().add(val);
        obj.get(name).getValue1().incrementAndGet();
        obj.get(name).getValue2().clear(); // may break the snapshot algorithm if a process do not replace an embedded_snap
        obj.get(name).getValue2().addAll(embedded_snap);
    }

    public Set read() {
        List<Set> list = snap();

        Set result = new Set();

        for (Set ens : list) {
            result.add(ens);
        }
        return result;
    }
}
