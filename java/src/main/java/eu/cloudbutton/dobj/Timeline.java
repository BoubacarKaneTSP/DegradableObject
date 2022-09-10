package eu.cloudbutton.dobj;

import eu.cloudbutton.dobj.incrementonly.BoxLong;
import eu.cloudbutton.dobj.incrementonly.Counter;

import java.util.*;

public class Timeline<T> {

    private final static int LENGTH=50;

    private final ThreadLocal<Queue<T>> topk;
    private final Queue<T> timeline;
    private final Counter counter;
    private final ThreadLocal<BoxLong> lastTimelineSize;

    public Timeline(Queue timeline, Counter counter) {
        this.timeline = timeline;
        this.counter = counter;
        lastTimelineSize = ThreadLocal.withInitial(BoxLong::new);
        topk = ThreadLocal.withInitial((LinkedList::new));
    }

    public void add(T elt){
        timeline.offer(elt);
        counter.incrementAndGet();
   }

   public Queue<T> read(){
        long nbEltAdded, counterValue;

        counterValue = counter.read();
        nbEltAdded = counterValue - lastTimelineSize.get().getVal();

        lastTimelineSize.get().setVal(counterValue);

        for (int i = 0; i < nbEltAdded; i++)
            topk.get().add(timeline.poll());

        int topkSize = topk.get().size();
        for (int i = 0; i < topkSize - LENGTH; i++)
            topk.get().poll();

        return topk.get();
   }
}
