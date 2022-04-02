package eu.cloudbutton.dobj.Benchmark.Tester;

import java.util.Deque;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;

public class DequeTester extends Tester<Deque> {

    public DequeTester(Deque list, int[] ratios, CountDownLatch latch) {
        super(list, ratios, latch);
    }

    @Override
    protected void test(opType type, long iid) {

        switch (type) {
            case ADD:
                object.addFirst(iid);
                break;
            case REMOVE:
                try{
                    object.removeLast();
                } catch (NoSuchElementException e) {
                }
                break;
            case READ:
                object.toArray();
                break;
        }
    }
}
