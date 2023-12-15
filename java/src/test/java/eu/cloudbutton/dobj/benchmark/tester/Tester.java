package eu.cloudbutton.dobj.benchmark.tester;

import eu.cloudbutton.dobj.benchmark.Microbenchmark;
import eu.cloudbutton.dobj.benchmark.Microbenchmark.opType;
import eu.cloudbutton.dobj.incrementonly.BoxedLong;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Tester<T> implements Callable<Void> {

    protected static final int ITEM_PER_THREAD = 1000;
    protected final ThreadLocalRandom random;
    protected final T object;
    protected final int[] ratios;
    protected final CountDownLatch latch;
    protected int nbRepeat;

    public Tester(T object, int[] ratios, CountDownLatch latch) {
        this.random = ThreadLocalRandom.current();
        this.object = object;
        this.ratios = ratios;
        this.latch = latch;
        this.nbRepeat = 1;
    }

    @Override
    public Void call() {

        int opNumber = 0;
        long n, elapsedTime;
        List<BoxedLong> localOp = new ArrayList<>();
        List<BoxedLong> localTimeOp = new ArrayList<>();

        for (opType ignored : opType.values()){
            localOp.add(new BoxedLong());
            localTimeOp.add(new BoxedLong());
        }

        try{
            // warm up
            opType type;
            while (Microbenchmark.flag.get()) {

                n = this.random.nextInt(100);

                if (n < ratios[0]) {
                    type = opType.ADD;
                }else if(n < ratios[0] + ratios[1]) {
                    type = opType.REMOVE;
                }else {
                    type = opType.READ;
                }
                test(type);
            }

            latch.countDown();
            latch.await();

            // compute
            while (!Microbenchmark.flag.get()) {
                n = this.random.nextInt(100);

                if (n < ratios[0]) {
                    type = opType.ADD;
                }else if(n < ratios[0] + ratios[1]) {
                    type = opType.REMOVE;
                }else {
                    type = opType.READ;
                }

                elapsedTime = test(type);

                assert elapsedTime !=0 ;

                switch (type){
                    case ADD:
                        opNumber = 0;
                        break;
                    case REMOVE:
                        opNumber = 1;
                        break;
                    case READ:
                        opNumber = 2;
                        break;
                }

                // if (elapsedTime != 0)
                localOp.get(opNumber).val += nbRepeat;
                localTimeOp.get(opNumber).val += elapsedTime;

                // simulate I/O
                // Thread.sleep(0,5000);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        opNumber = 0;

        for (opType ignored : opType.values()){
            Microbenchmark.nbOperations.get(opNumber).addAndGet(localOp.get(opNumber).getVal());
            Microbenchmark.timeOperations.get(opNumber).addAndGet(localTimeOp.get(opNumber).getVal());
            opNumber++;
        }

        return null;
    }

    protected abstract long test(opType type) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException;

}
