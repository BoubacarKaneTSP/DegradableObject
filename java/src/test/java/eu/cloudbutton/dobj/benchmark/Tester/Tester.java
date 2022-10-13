package eu.cloudbutton.dobj.benchmark.Tester;

import eu.cloudbutton.dobj.benchmark.Microbenchmark;
import eu.cloudbutton.dobj.benchmark.Microbenchmark.opType;
import eu.cloudbutton.dobj.incrementonly.BoxedLong;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
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
        this.nbRepeat = 1000;
    }

    @Override
    public Void call() {

        long n, elapsedTime;
        Map<opType, BoxedLong> localOp = new HashMap<>();
        Map<opType, BoxedLong> localTimeOp = new HashMap<>();

        ThreadLocal<BoxedLong> threadLocal = ThreadLocal.withInitial(BoxedLong::new);


        for (opType type: opType.values()){
            localOp.put(type, new BoxedLong());
            localTimeOp.put(type, new BoxedLong());
        }
        latch.countDown();

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

                if (elapsedTime != 0)
                    localOp.get(type).val += nbRepeat;

                localTimeOp.get(type).val += elapsedTime;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        for (opType type: opType.values()){
            Microbenchmark.nbOperations.get(type).addAndGet(localOp.get(type).getVal());
            Microbenchmark.timeOperations.get(type).addAndGet(localTimeOp.get(type).getVal());
        }

        return null;
    }

    protected abstract long test(opType type) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException;
    protected abstract long test(opType type, BoxedLong boxedLong) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException;
}
