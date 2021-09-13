package eu.cloudbutton.dobj.types;

import java.util.AbstractQueue;
import java.util.Iterator;

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

   public String read(){

       Iterator it = timeline.iterator();
       String s = "[";
       Object o;
       for (int i = 0; i < 1000 && it.hasNext(); i++) {
           o = it.next();
           s = s + o.toString();
           if(it.hasNext())
               s += ", ";
       }

       s = s + "]";

       return s;
   }
}
