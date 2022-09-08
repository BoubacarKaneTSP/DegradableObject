package eu.cloudbutton.dobj;

import java.util.*;

public class Timeline<T> {

    private final static int LENGTH=50;

    private final LinkedList<T> topk = new LinkedList<>();
    private final Queue<T> timeline;

    public Timeline(Queue timeline) {
        this.timeline = timeline;
    }

    public void add(T elt){
        timeline.offer(elt);
   }

   public List<T> read(){
       T e = timeline.poll();
       if (e!=null) {
           topk.add(e);
           if (topk.size()>LENGTH)
               topk.poll();
       }
       return topk;
   }
}
