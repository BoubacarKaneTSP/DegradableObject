package eu.cloudbutton.dobj.types;

import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ListSnapshot extends AbstractListSnapshot<List>{

    public ListSnapshot() {
        super();
    }

    @Override
    protected void write(String val) {
        String name = Thread.currentThread().getName();

        if (!obj.containsKey(name)){
            obj.put(name, new Triplet<>(new List(), new AtomicInteger(), new ArrayList<>()));
        }
        java.util.List<List> embedded_snap = snap();
        obj.get(name).getValue0().append(val);
        obj.get(name).getValue1().incrementAndGet();
        obj.get(name).getValue2().clear(); // may break the snapshot algorithm if a process do not replace an embedded_snap
        obj.get(name).getValue2().addAll(embedded_snap);
    }

    @Override
    protected List read() {
        java.util.List<List> list = snap();

        List result = new List();

        for (List count : list) {
            result.append(count);
        }
        return result;
    }
}
