package eu.cloudbutton.dobj;

import lombok.Getter;

import java.util.*;

public class Timeline<T> {

    private final static int LENGTH=10;

    private final Queue<T> topk;
    @Getter
    private final Queue<T> timeline;

    public Timeline(Queue timeline) {
        this.timeline = timeline;
        topk = new LinkedList<>();
    }

    public void add(T elt) throws InterruptedException {
        timeline.offer(elt);
   }

   public Queue<T> read() throws InterruptedException {

       long queueSize = timeline.size();

       for (int i = 0; i < queueSize; i++)
           topk.add(timeline.poll());

       int topkSize = topk.size();

       for (int i = 0; i < topkSize - LENGTH; i++)
           topk.poll();

//       timeline.clear();

       return topk;
   }

   public void clear(){
        timeline.clear();
   }
}
