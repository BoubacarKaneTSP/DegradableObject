package eu.cloudbutton.dobj.types;

import org.javatuples.Pair;

import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FasterSnapshotSRMW<T> {

    private volatile int curr_seq;
    protected final ConcurrentMap<Thread, Pair<T, Integer>> memory;

    public FasterSnapshotSRMW(){
        curr_seq = 0;
        memory = new ConcurrentHashMap<>();
    }

    protected void update(T val){
        memory.put(Thread.currentThread(),
                new Pair(
                        curr_seq,
                        val)
                );
    }

    protected List<T> snap(){
//        curr_seq += 1;
//        List<Pair<T,Integer>> view = new ArrayList<>(memory.size());
//        for (Pair<T,Integer> p : memory.values()) {
//            view.add(p);
//        }
//        curr_seq += 1;
//        List<Pair<T,Integer>> nview = new ArrayList<>(memory.size());
//        for (Pair<T,Integer> p : memory.values()) {
//            nview.add(p);
//        }
//
//        for (Pair<T,Integer> p : nview) {
//            if ()
//        }
//
//        return view;
        return null;
    }
}
