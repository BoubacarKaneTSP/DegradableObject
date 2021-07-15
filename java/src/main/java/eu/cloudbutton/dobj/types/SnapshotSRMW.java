package eu.cloudbutton.dobj.types;

import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SnapshotV2<T> {

    private int curr_seq;
    protected final ConcurrentMap<Thread, Pair<Pair<T, Integer>, Pair<T, Integer>>> memory; //Value0 = low, Value1 = high

    public SnapshotV2(){
        curr_seq = 0;
        memory = new ConcurrentHashMap<>();
    }

    protected void update(T val){

        int seq = curr_seq;
        Pair<T, Integer> high_r = memory.get(Thread.currentThread()).getValue1();
        if (seq != high_r.getValue1())
            memory.replace(Thread.currentThread(),memory.get(Thread.currentThread()).setAt0(high_r));

        memory.put(Thread.currentThread(),
                new Pair<>(
                        memory.get(Thread.currentThread()).getValue0(),
                        memory.get(Thread.currentThread()).getValue1().setAt0(val).setAt1(seq))
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
