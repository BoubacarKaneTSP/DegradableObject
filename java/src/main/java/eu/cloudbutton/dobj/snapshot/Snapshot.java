package eu.cloudbutton.dobj.snapshot;

import org.javatuples.Triplet;


import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Snapshot<T> {

    protected final ConcurrentMap<Thread, Triplet<T, AtomicInteger, List<T>>> obj;
    private final ThreadLocal<Set<Triplet<T, AtomicInteger, List<T>>>> read1;
    private final ThreadLocal<Set<Triplet<T, AtomicInteger, List<T>>>> read2;

    public Snapshot(ConcurrentMap<Thread, Triplet<T, AtomicInteger, List<T>>> obj) {
        this.obj = obj;
        this.read1 = new ThreadLocal<>();
        this.read2 = new ThreadLocal<>();
    }

    protected List<T> snap(){

        Map<Thread, List<Triplet<T, AtomicInteger, List<T>>>> reads = new HashMap<>(); //Keep track of the last 4 different tags

        this.read1.set(new HashSet<>());
        this.read2.set(new HashSet<>());

        Triplet<T, AtomicInteger, List<T>> t;
                
        int flag = 0;

        while (flag == 0){

            read1.get().clear();

            for (Thread process : obj.keySet()) { // This loop goes through each process' tags
                // Maybe reset read1 here
                t = obj.get(process);

                if (!reads.containsKey(process)) { // The first time we read a process' object, we create the list of tags
                    List<Triplet<T, AtomicInteger, List<T>>> tags = new ArrayList<>();
                    tags.add(t);
                    reads.put(process, tags);
                }
                else{
                    List<Triplet<T, AtomicInteger, List<T>>> l = reads.get(process);
                    if (!l.contains(t))
                        l.add(t);

                    if (l.size() == 4) {
                        Triplet<T, AtomicInteger, List<T>> result = l.get(2);
                        return result.getValue2();
                    }
                }

                read1.get().add(t);
            }

            if (read1.get().equals(read2.get()))
                flag = 1;
            else
                read2.set(Set.copyOf(read1.get()));
        }

        List<T> result = new ArrayList<>();

        for (Triplet<T, AtomicInteger, List<T>> triplet: read1.get())
            result.add(triplet.getValue0());

        return result;
    }
}
