package eu.cloudbutton.dobj.types;

public class FuzzyCounter extends AbstractCounter{
    
    private final AbstractCounter counter;
    private int N; // Base increment based on the number of thread
    private final ThreadLocal<Long> ID;
    
    public FuzzyCounter(){
        counter = new DegradableCounter();
        N = 1;
        ID = ThreadLocal.withInitial(() -> Thread.currentThread().getId());
    }
    
    @Override
    public void increment() {
        counter.increment();
    }

    @Override
    public void increment(int delta) {
    }

    @Override
    public long read() {

        long res = counter.read() * N;

        String valString = Long.toString(res);
        valString = valString + ID.get();
        return Long.valueOf(valString);
    }

    public void setN(int nbThread)   {
        N = nbThread;
    }
}
