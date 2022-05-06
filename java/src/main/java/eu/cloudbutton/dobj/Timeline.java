package eu.cloudbutton.dobj;

import eu.cloudbutton.dobj.counter.AbstractCounter;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Timeline<T> {

    private final AbstractQueue<T> timeline;
    private final AbstractCounter size;
    private AtomicBoolean flag = new AtomicBoolean();

    public Timeline(AbstractQueue timeline, AbstractCounter size) {
        this.timeline = timeline;
        this.size = size;
        this.flag.set(true);
    }

    public void add(T elt){

        if (flag.get()){
            size.incrementAndGet();
            if (size.read() >= 50) {
                flag.set(false);
            }
            timeline.offer(elt);
        }else{
            timeline.offer(elt);
            timeline.poll();
        }
   }

   public AbstractQueue<T> read(){

       /*Collection<T> ret = new ArrayList<>();

       Iterator<T> it = timeline.iterator();
       int i = 0;

       while (it.hasNext() && i < 50) {
           ret.add(it.next());
           i++;
       }*/
       return timeline;
   }
}
