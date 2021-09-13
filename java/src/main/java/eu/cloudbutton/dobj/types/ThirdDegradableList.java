package eu.cloudbutton.dobj.types;

import org.javatuples.Pair;

import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ThirdDegradableList<T> extends AbstractQueue<T> implements Queue<T>{
    private final ConcurrentMap<Thread, ConcurrentSkipListMap<Integer,Pair<Integer, T>>> list;
    private final ConcurrentMap<Thread,Integer> last;
    private final ThreadLocal<ConcurrentSkipListMap<Integer, Pair<Integer, T>>> local_map;
    private final ThreadLocal<AtomicInteger> local_num_add;
    private final AtomicInteger count;
    private final List<T> list_final;
    private final List<Pair<Integer, T>> inter_list;

    public ThirdDegradableList() {
        this.list = new ConcurrentHashMap<>();
        this.last = new ConcurrentHashMap<>();
        this.local_map = ThreadLocal.withInitial(() -> {
            ConcurrentSkipListMap<Integer, Pair<Integer,T>> l = new ConcurrentSkipListMap<>();
            list.put(Thread.currentThread(), l);
            return l;
        });
        this.local_num_add = ThreadLocal.withInitial(AtomicInteger::new);
        this.count = new AtomicInteger();
        this.list_final = new ArrayList<>();
        this.inter_list = new ArrayList<>();
    }

    @Override
    public Iterator<T> iterator() {
        readlist();
        return list_final.iterator();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean add(T val) {
        local_map.get().put(local_num_add.get().incrementAndGet(), new Pair<>(count.incrementAndGet() ,val));
        return true;
    }

    public java.util.List<T> read() {
        readlist();
        return list_final;

    }


    @Override
    public boolean remove(Object val){
        throw new java.lang.Error("Remove not build yet");
    }

    @Override
    public boolean contains(Object val) {

        for ( ConcurrentSkipListMap<Integer, Pair<Integer, T>> map : list.values()){
            for (Pair<Integer, T> i : map.values())
                if(i.getValue1() == val)
                    return true;

        }

        return false;
    }

    @Override
    public void clear() {
        throw new java.lang.Error("Clear method not build yet");
    }

    @Override
    public boolean offer(T t) {
        return false;
    }

    @Override
    public T poll() {
        return null;
    }

    @Override
    public T peek() {
        return null;
    }

    @Override
    public String toString(){
        readlist();
        return list_final.toString();
    }

    private void readlist() {
        inter_list.clear();
        for (Thread key : this.list.keySet()) {

            int lastkey = list.get(key).lastKey();
            if (!last.containsKey(key)) {
                last.put(key, lastkey);
                for (Map.Entry<Integer, Pair<Integer, T>> elem : list.get(key).headMap(lastkey).entrySet())
                    inter_list.add(elem.getValue());
            } else if (last.get(key) != lastkey) {
                int i;
                for (i = last.get(key) + 1; i <= lastkey; i++)
                    inter_list.add(list.get(key).get(i));
                last.put(key, i);
            }


        }

        SortedMap<Integer, T> sortedMap = new TreeMap<>();

        for (Pair<Integer,T> pair : inter_list)
            sortedMap.put(pair.getValue0(), pair.getValue1());

        list_final.addAll(sortedMap.values());
    }
}
