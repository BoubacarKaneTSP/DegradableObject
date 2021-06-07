package eu.cloudbutton.dobj.types;

import org.javatuples.Pair;

import java.util.*;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ThirdDegradableList<T> extends AbstractList<T>{
    private final ConcurrentMap<String, ConcurrentSkipListMap<Integer,Pair<Integer, T>>> list;
    private final ConcurrentMap<String,Integer> last;
    private final ThreadLocal<ConcurrentSkipListMap<Integer, Pair<Integer, T>>> local_map;
    private final ThreadLocal<AtomicInteger> local_num_add;
    private final AtomicInteger count;
    private final List<T> list_final;
    private final List<Pair<Integer, T>> inter_list;

    public ThirdDegradableList() {
        this.list = new ConcurrentHashMap<>();
        this.last = new ConcurrentHashMap<>();
        this.local_map = new ThreadLocal<>();
        this.local_num_add = new ThreadLocal<>();
        this.count = new AtomicInteger();
        this.list_final = new ArrayList<>();
        this.inter_list = new ArrayList<>();
    }

    @Override
    public void append(T val) {
        String pid = Thread.currentThread().getName();
        if(!list.containsKey(pid)){
            local_map.set(new ConcurrentSkipListMap<>());
            local_num_add.set(new AtomicInteger());

            this.list.put(pid, local_map.get());
        }

        local_map.get().put(local_num_add.get().incrementAndGet(), new Pair<>(count.incrementAndGet() ,val));
    }

    @Override
    public java.util.List<T> read() {
        inter_list.clear();
        for (String key : this.list.keySet()) {

            int lastkey = list.get(key).lastKey();
            if (!last.containsKey(key)) {
                last.put(key, lastkey);
                for (Map.Entry<Integer, Pair<Integer, T>> elem : list.get(key).headMap(lastkey).entrySet())
                    inter_list.add(elem.getValue());
            } else {
                if (last.get(key) != lastkey) {
                    int i;
                    for (i = last.get(key) + 1; i <= lastkey; i++)
                        inter_list.add(list.get(key).get(i));
                    last.put(key, i);
                }
            }

        }

        SortedMap<Integer, T> sortedMap = new TreeMap<>();

        for (Pair<Integer,T> pair : inter_list)
            sortedMap.put(pair.getValue0(), pair.getValue1());

        list_final.addAll(sortedMap.values());

        return list_final;
    }

    public java.util.List<T> read(Set following) {
        inter_list.clear();
        for (String key : this.list.keySet()){
            if (following.contains(key)) {
                int lastkey = list.get(key).lastKey();
                if (!last.containsKey(key)) {
                    last.put(key, lastkey);
                    for (Map.Entry<Integer, Pair<Integer, T>> elem : list.get(key).headMap(lastkey).entrySet())
                        inter_list.add(elem.getValue());
                } else {
                    if (last.get(key) != lastkey) {
                        int i;
                        for (i = last.get(key) + 1; i <= lastkey; i++)
                            inter_list.add(list.get(key).get(i));
                        last.put(key, i);
                    }
                }
            }
        }

        SortedMap<Integer, T> sortedMap = new TreeMap<>();

        for (Pair<Integer,T> pair : inter_list)
            sortedMap.put(pair.getValue0(), pair.getValue1());

        list_final.addAll(sortedMap.values());

        return list_final;
    }


    @Override
    public void remove(T val){
        throw new java.lang.Error("Remove not build yet");
    }

    @Override
    public boolean contains(T val) {

        for ( ConcurrentSkipListMap<Integer, Pair<Integer, T>> map : list.values()){
            for (Pair<Integer, T> i : map.values())
                if(i.getValue1() == val)
                    return true;

        }

        return false;
    }
}
