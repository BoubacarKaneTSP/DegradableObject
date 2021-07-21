package eu.cloudbutton.dobj.types;

public class Counter extends AbstractCounter {

    private int count;

    public Counter(Counter counter) { count = counter.read(); }
    public Counter(int i) { count = i ; }
    public Counter() { count = 0; }

    @Override
    public void increment() {
        count++ ;
    }

    public void increment(int val) {
        count += val;
    }

    @Override
    public int read() {
        return count;
    }

    @Override
    public void write() {
        increment();
    }

    @Override
    public void write(int val) {
        increment(val);
    }
}
