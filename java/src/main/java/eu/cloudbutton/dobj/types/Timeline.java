package eu.cloudbutton.dobj.types;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Timeline<T> {

    private final AbstractCollection<T> timeline;
    private final Integer size;

    public Timeline(AbstractCollection<T> timeline, Integer size) {
        this.timeline = timeline;
        this.size = size;

    }

    public void add(T elt){
        timeline.add(elt);
   }

   public Collection<T> read(){

       Collection<T> ret = new ArrayList<>();

       Iterator<T> it = timeline.iterator();
       int i = 0;

       while (it.hasNext() && i < 50) {
           ret.add(it.next());
           i++;
       }
       return ret;
   }
}
