package eu.cloudbutton.dobj.Benchmark.Tester;

import java.util.AbstractQueue;

public class QueueFiller extends Filler<AbstractQueue> {

    public QueueFiller(AbstractQueue object, long nbOps) {
        super(object, nbOps);
    }

    @Override
    public void fill() {

        for (int i = 0; i < nbOps; i++) {
            object.add(i);
        }

    }
}
