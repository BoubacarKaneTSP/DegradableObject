package eu.cloudbutton.dobj.Benchmark.Tester;

import eu.cloudbutton.dobj.types.Noop;

import java.util.concurrent.CountDownLatch;

public class NoopTester extends Tester<Noop> {

    public NoopTester(Noop nope, int[] ratios, CountDownLatch latch, long nbOps) {
        super(nope, ratios, latch, nbOps);
    }

    @Override
    protected void test(opType type) {
        // no-op
        int n = random.nextInt(ITEM_PER_THREAD);
        switch (type) {
            case ADD:
                n++;
                break;
            case REMOVE:
                n--;
                break;
            case READ:
                n += 2;
                break;
        }
    }
}
