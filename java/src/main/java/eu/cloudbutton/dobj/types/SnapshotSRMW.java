package eu.cloudbutton.dobj.types;

import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SnapshotSRMW<T> {

    private volatile int curr_seq;
    protected final ConcurrentMap<Integer, Pair<Pair<T, Integer>, Pair<T, Integer>>> memory; //Value0 = low, Value1 = high
    private final ThreadLocal<Integer> name;

    public SnapshotSRMW(){
        curr_seq = 0;
        memory = new ConcurrentHashMap<>();
        name = new ThreadLocal<>();
    }

    protected void update(T val){
        if (name.get() == null)
            name.set(Integer.parseInt(Thread.currentThread().getName().substring(5).replace("-thread-","")));

        int seq = curr_seq;
        Pair<T, Integer> high_r = memory.get(name.get()).getValue1();
        if (seq != high_r.getValue1())
            memory.replace(name.get(),memory.get(name.get()).setAt0(high_r));

        memory.replace(name.get(),
                new Pair<>(
                        memory.get(name.get()).getValue0(),
                        memory.get(name.get()).getValue1().setAt0(val).setAt1(seq))
                );

    }

    protected List<T> snap(){
        curr_seq += 1;
        List<T> view = new ArrayList<>();

        for (Pair register : memory.values()) {
            Pair<T,Integer> high_r = (Pair<T, Integer>) register.getValue1();
            if (high_r.getValue1() < curr_seq)
                view.add(high_r.getValue0());
            else
                view.add(((Pair<T,Integer>)register.getValue0()).getValue0());
        }
        return view;
    }
}
