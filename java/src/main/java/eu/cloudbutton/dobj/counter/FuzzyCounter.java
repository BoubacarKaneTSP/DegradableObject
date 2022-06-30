package eu.cloudbutton.dobj.counter;

import lombok.SneakyThrows;

public class FuzzyCounter extends AbstractCounter{
    
    private final DegradableCounter counter;
    private int N; // Base increment based on the number of thread
    private final ThreadLocal<Long> ID;
    
    public FuzzyCounter(){
        counter = new DegradableCounter();
        N = 1;
        ID = ThreadLocal.withInitial(() -> Thread.currentThread().getId());
    }
    
    @Override
    public long incrementAndGet() {
        counter.incrementAndGet();
		
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

    public void setN(int nbThread)   {
        N = nbThread;
    }
}
