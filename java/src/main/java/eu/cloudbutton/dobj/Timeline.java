package eu.cloudbutton.dobj;

import java.util.*;

public class Timeline<T> {

    private final static int LENGTH=50;

    private final ThreadLocal<Queue<T>> topk;
    private final Queue<T> timeline;

    public Timeline(Queue timeline) {
        this.timeline = timeline;
        topk = ThreadLocal.withInitial((LinkedList::new));
    }

    public void add(T elt) throws InterruptedException {
        timeline.offer(elt);
   }

   public Queue<T> read() throws InterruptedException {

       long queueSize = timeline.size();

       for (int i = 0; i < queueSize; i++)
           topk.get().add(timeline.poll());

       int topkSize = topk.get().size();

        for (int i = 0; i < topkSize - LENGTH; i++)
            topk.get().poll();

        return topk.get();
   }
}
