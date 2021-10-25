package eu.cloudbutton.dobj.types;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;

public class Timeline<T> {

    private final AbstractQueue<T> timeline;
    private final AbstractCounter size;

    public Timeline(AbstractQueue<T> timeline, AbstractCounter size) {
        this.timeline = timeline;
        this.size = size;
    }

    public void add(T elt){
        timeline.add(elt);
        size.increment();
   }

   public Collection<T> read(){

       Collection<T> ret = new ArrayList<>();
       Iterator<T> it = timeline.iterator();
       for (int i = 0; i < 50 && it.hasNext(); i++) {
           if(it.hasNext())
	       ret.add(it.next());
       }

       return ret;
   }
}
