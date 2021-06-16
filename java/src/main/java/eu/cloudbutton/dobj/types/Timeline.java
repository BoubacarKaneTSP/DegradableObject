package eu.cloudbutton.dobj.types;

import org.javatuples.Pair;

import java.util.concurrent.ConcurrentMap;

public class Timeline<T> extends ThirdDegradableList {

    private final T timeline;

    public Timeline(T timeline) {
        this.timeline = timeline;

    }

    public void add(String process, ConcurrentMap<Integer, Pair<Integer, T>> v){

   }
}
