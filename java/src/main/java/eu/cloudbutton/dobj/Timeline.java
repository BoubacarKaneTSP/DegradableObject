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
    private AtomicBoolean flagSizeCapacity;
    private AtomicInteger currentSize;

    public Timeline(Queue<T> timeline) {
        this.timeline = timeline;
        topk = new LinkedList<>();
//        flagSizeCapacity = new AtomicBoolean(false);
//        this.currentSize = new AtomicInteger();
    }

    public void add(T elt) throws InterruptedException {

        timeline.offer(elt);

   }
//   public void add(T elt) throws InterruptedException {
//
//        if (!flagSizeCapacity.get()){
//            if(currentSize.getAndIncrement() > CAPACITY){
//                flagSizeCapacity.set(true);
//                timeline.offer(elt);
//                timeline.poll();
//            }else{
//                timeline.offer(elt);
//            }
//        }else{
//            timeline.offer(elt);
//            timeline.poll();
//        }
//   }

   public Queue<T> read() throws InterruptedException {

       long queueSize = timeline.size();

       System.out.println("queue size : " + queueSize);
       for (int i = 0; i < queueSize; i++)
           System.out.println("i : " + i);
           topk.add(timeline.poll());

       int topkSize = topk.size();

        for (int i = 0; i < topkSize - CAPACITY; i++)
            topk.poll();


       return topk;
   }

   public void clear(){
        timeline.clear();
        topk.clear();
   }
}
