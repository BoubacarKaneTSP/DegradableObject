package eu.cloudbutton.dobj;

import eu.cloudbutton.dobj.Counter.AbstractCounter;
import org.apache.log4j.helpers.BoundedFIFO;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class Timeline<T> {

    private final static int LENGTH=50;

    private final LinkedList<T> topk = new LinkedList<>();
    private final AbstractQueue<T> timeline;

    public Timeline(AbstractQueue timeline, AbstractCounter size) {
        this.timeline = timeline;
        this.flag.set(true);
    }

    public void add(T elt){
        timeline.offer(elt);
   }

   public List<T> read(){
       T e = timeline.poll();
       if (e!=null) {
           topk.add(e);
           if (topk.size()>50)
               topk.poll();
       }
       return topk;
   }
}
