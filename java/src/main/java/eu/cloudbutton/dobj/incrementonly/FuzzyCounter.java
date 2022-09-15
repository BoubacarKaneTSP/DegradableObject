package eu.cloudbutton.dobj.incrementonly;

public class FuzzyCounter implements Counter {
    
    private final CounterIncrementOnly counter;
    private int N; // Base increment based on the number of thread
    private final ThreadLocal<Long> ID;
    
    public FuzzyCounter(){
        counter = new CounterIncrementOnly();
        N = 1;
        ID = ThreadLocal.withInitial(() -> Thread.currentThread().getId());
    }
    
    @Override
    public long incrementAndGet() {
        counter.increment();
		
	return (counter.read() * N) + ID.get();
    }

    @Override
    public long addAndGet(int delta) {
        counter.addAndGet(delta);

        return (counter.read() * N) + ID.get();
    }

    @Override
    public long read() {

        return (counter.read() * N) + ID.get();
    }

    @Override
    public long decrementAndGet(int delta) {
        return 0;
    }

    @Override
    public long decrementAndGet() {
        return 0;
    }

    public void setN(int nbThread)   {
        N = nbThread;
    }
}
