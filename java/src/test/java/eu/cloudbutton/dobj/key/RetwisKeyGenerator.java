package eu.cloudbutton.dobj.key;

import nl.peterbloem.powerlaws.DiscreteApproximate;
import org.apache.commons.math3.distribution.ParetoDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.RandomGeneratorFactory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RetwisKeyGenerator implements KeyGenerator {


    private final List<Long> list;
    private ThreadLocal<Random> random;
    protected final int bound;

    public RetwisKeyGenerator(int max_hashes_per_thread, int nbUsers, double alpha) {
        this.random = ThreadLocal.withInitial(() -> new Random(System.nanoTime()+Thread.currentThread().getId()));
        this.bound = max_hashes_per_thread;
        this.list =  new ArrayList<>();
        fill(nbUsers, alpha);
    }
    public RetwisKeyGenerator(int max_hashes_per_thread) {
        this.random = ThreadLocal.withInitial(() -> new Random(System.nanoTime()+Thread.currentThread().getId()));
        this.bound = max_hashes_per_thread;
        this.list =  new ArrayList<>();
        fill();
    }

    @Override
    public Key nextKey() {
        return  new CollidingKey(
                Thread.currentThread().getId(),
                random.get().nextInt(Integer.MAX_VALUE),
                list.get(random.get().nextInt(bound)),
                bound
        );
    }

    private void fill(int nbUsers, double alpha){

        double SCALE = 1.0;
        double SHAPE = alpha;
        int numValues = nbUsers;

        List<Double> doubleValues = new ArrayList<>();

        RandomGenerator rand = RandomGeneratorFactory.createRandomGenerator(new Random(94));
        ParetoDistribution distribution = new ParetoDistribution(rand,SCALE,SHAPE);

        double maxGeneratedValue = 0;
        for (int i = 0; i < numValues; i++) {
            double randomValue = distribution.sample();
            doubleValues.add(randomValue);
            if (randomValue > maxGeneratedValue) {
                maxGeneratedValue = randomValue;
            }
        }

        double scaleFactor = numValues / maxGeneratedValue;

        for (int i = 0; i < numValues; i++) {
            double scaledValue = doubleValues.get(i) * scaleFactor;
            list.add(Math.round(scaledValue));
        }
    }

    private void fill(){

        double SCALE = 1.0;
        double SHAPE = 1.35;
        int numValues = 100;

        List<Double> doubleValues = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        RandomGenerator rand = RandomGeneratorFactory.createRandomGenerator(new Random(94));
        ParetoDistribution distribution = new ParetoDistribution(rand,SCALE,SHAPE);

        double maxGeneratedValue = 0;
        for (int i = 0; i < numValues; i++) {
            double randomValue = distribution.sample();
            doubleValues.add(randomValue);
            if (randomValue > maxGeneratedValue) {
                maxGeneratedValue = randomValue;
            }
        }

        double scaleFactor = numValues / maxGeneratedValue;

        for (int i = 0; i < numValues; i++) {
            double scaledValue = doubleValues.get(i) * scaleFactor;
            list.add(Math.round(scaledValue));
        }
    }

    private static class CollidingKey extends ThreadLocalKey implements Comparable<ThreadLocalKey> {

        private long hash;

        CollidingKey(long tid, long id, long hash, int max_hashes_per_thread) {
            super(tid,id, max_hashes_per_thread);
            this.hash = hash;
        }

        @Override
        public int hashCode() {
            return (int) hash;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CollidingKey that = (CollidingKey) o;
            return tid == that.tid && id == that.id && hash == that.hash;
        }

        @Override
        public int compareTo(@NotNull ThreadLocalKey key) {
            CollidingKey key1 = (CollidingKey) key;
            if (id>key1.id) return 1;
            else if (id<key1.id) return -1;
            else if (tid>key1.tid) return 1;
            else if (tid<key1.tid) return -1;
            else if (hash>key1.hash) return 1;
            else if (hash<key1.hash) return -1;
            return 0;
        }

        public String toString() {
            return "("+tid+","+id+","+hash+")";
        }
    }
}
