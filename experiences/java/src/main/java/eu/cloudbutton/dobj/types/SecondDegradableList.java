package eu.cloudbutton.dobj.types;

import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SecondDegradableList<T> extends AbstractQueue<T> implements Queue<T> {

    private final ConcurrentMap<Thread, ConcurrentSkipListMap<Integer,T>> list;
    private final ThreadLocal<ConcurrentSkipListMap<Integer,T>> local;
    private final ThreadLocal<AtomicInteger> num_add;
    private final ConcurrentMap<Thread,Integer> last;
    private final List<T> list_final;

    public SecondDegradableList() {
        this.list = new ConcurrentHashMap<>();
        this.last = new ConcurrentHashMap<>();
        this.local = ThreadLocal.withInitial(() -> {
            ConcurrentSkipListMap<Integer, T> l = new ConcurrentSkipListMap<>();
            list.put(Thread.currentThread(), l);
            return l;
        });
        this.num_add = ThreadLocal.withInitial(AtomicInteger::new);
        this.list_final = new ArrayList<>();
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
        local.get().put(num_add.get().incrementAndGet(), val);
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
        for (ConcurrentSkipListMap<Integer, T> map : list.values()) {
            if(map.containsValue(val))
                return true;
        }
        return false;
    }

    @Override
    public void clear() {
        throw new java.lang.Error("Remove not build yet");
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
        return "method toString not build yet";
    }

    private void readlist() {
        for (Thread key : this.list.keySet()){
            int lastkey = list.get(key).lastKey();
            if (!last.containsKey(key)){
                try{
                    last.put(key, lastkey);
                    for (Map.Entry<Integer, T> elem : list.get(key).headMap(lastkey).entrySet())
                        list_final.add(elem.getValue());
                } catch (NoSuchElementException e) {
                    e.printStackTrace();
                }


            } else{
                if (last.get(key) != lastkey){
                    int i;
                    for ( i = last.get(key)+1; i <= lastkey ; i++)
                        list_final.add(list.get(key).get(i));
                    last.put(key, i);
                }
            }
        }
    }
}
