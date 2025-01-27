package eu.cloudbutton.dobj;

import lombok.Getter;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Timeline<T> {

    private final static int CAPACITY = 1000;

    private final Queue<T> topk;
    @Getter
    private final Queue<T> timeline;

    public Timeline(Queue<T> timeline) {
        this.timeline = timeline;
        topk = new LinkedList<>();
    }

    public void add(T elt)  {
        assert elt != null;
        timeline.offer(elt);
   }

   public Queue<T> read() {
       topk.clear();
       for (int i = 0; i < CAPACITY; i++) {
           T t=timeline.poll();
           if (t == null)
               break;
           topk.add(t);
       }
       return topk;
   }

   public int size(){
        return timeline.size();
   }

   public void clear(){
        timeline.clear();
   }
}
