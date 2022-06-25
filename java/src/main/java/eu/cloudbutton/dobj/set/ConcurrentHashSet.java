package eu.cloudbutton.dobj.set;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashSet<T> extends AbstractSet<T> {

    private final AbstractMap<T, Object> map;

    public ConcurrentHashSet() {
        this.map = new ConcurrentHashMap<>();
    }


    @Override
    public Iterator<T> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean add(T val){
        return map.put(val, 0) == null;
    }


    @Override
    public boolean remove(Object val){
        return map.remove(val) != null;
    }

    @Override
    public boolean contains(Object val) {
        return map.containsKey(val);
    }

    @Override
    public String toString(){
        return map.keySet().toString();
    }
}
