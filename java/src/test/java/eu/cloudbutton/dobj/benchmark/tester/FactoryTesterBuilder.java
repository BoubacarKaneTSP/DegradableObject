package eu.cloudbutton.dobj.benchmark.tester;

import java.util.concurrent.CountDownLatch;

public class FactoryTesterBuilder {

    private Object object;
    private int[] ratios;
    private CountDownLatch latch;
    private boolean useCollisionKey = false;
    private int max_item_per_thread = Integer.MAX_VALUE;

    public FactoryTesterBuilder() {}

    public FactoryTester buildTester(){
        return new FactoryTester(object,ratios,latch,useCollisionKey,max_item_per_thread);
    }

    public FactoryTesterBuilder object(Object object){
        this.object = object;
        return this;
    }

    public FactoryTesterBuilder ratios(int[] ratios){
        this.ratios = ratios;
        return this;
    }

    public FactoryTesterBuilder latch(CountDownLatch latch){
        this.latch = latch;
        return this;
    }

    public FactoryTesterBuilder useCollisionKey(boolean useCollisionKey){
        this.useCollisionKey = useCollisionKey;
        return this;
    }

    public FactoryTesterBuilder maxItemPerThread(int val){
        this.max_item_per_thread = val;
        return this;
    }
}
