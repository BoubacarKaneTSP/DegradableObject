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

    public void add(T elt) throws InterruptedException {
        assert elt != null;
        timeline.offer(elt);
   }

   public Queue<T> read() throws InterruptedException {
       long queueSize = timeline.size();
       for (int i = 0; i < queueSize; i++) {
           T t=timeline.poll();
           assert t !=  null;
           topk.add(t);
       }
       for (int i = 0; i < topk.size() - CAPACITY; i++)
           topk.poll();
       return topk;
   }

   public void clear(){
        timeline.clear();
        topk.clear();
   }
}
