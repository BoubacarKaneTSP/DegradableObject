package eu.cloudbutton.dobj.Benchmark.Tester;

import eu.cloudbutton.dobj.types.Noop;

import java.util.concurrent.CountDownLatch;

public class NoopTester extends Tester<Noop> {

    public NoopTester(Noop nope, int[] ratios, CountDownLatch latch, long nbOps) {
        super(nope, ratios, latch, nbOps);
    }

    @Override
    protected void test(char type) {
        // no-op
        int n = random.nextInt(ITEM_PER_THREAD);
        switch (type) {
            case 'a':
                n++;
                break;
            case 'r':
                n--;
                break;
            case 'c':
                n += 2;
                break;
        }
    }
}
