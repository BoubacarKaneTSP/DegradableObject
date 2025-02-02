package eu.cloudbutton.dobj.key;

import org.apache.commons.math3.distribution.ParetoDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.RandomGeneratorFactory;

import java.util.*;

public class RetwisKeyGenerator implements KeyGenerator {


    private final List<Long> list;
    private ThreadLocal<Random> random;
    protected final int bound;

    public RetwisKeyGenerator(int max_hashes_per_thread, int nbUsers, double alpha) {
        this.random = ThreadLocal.withInitial(() -> new Random(System.nanoTime()+Thread.currentThread().getId()));
        this.bound = max_hashes_per_thread;
        this.list =  new ArrayList<>();
        fill(nbUsers, alpha);
    //    Collections.sort(list);
    //    System.out.println(countOccurrences(list));
    }
    public RetwisKeyGenerator(int max_hashes_per_thread) {
        this.random = ThreadLocal.withInitial(() -> new Random(System.nanoTime()+Thread.currentThread().getId()));
        this.bound = max_hashes_per_thread;
        this.list =  new ArrayList<>();
        fill();
    }

    public static Map<Long, Integer> countOccurrences(List<Long> arrayList) {
        // Créer une HashMap pour stocker les valeurs et leurs occurrences
        Map<Long, Integer> occurrenceMap = new HashMap<>();

        // Parcourir l'ArrayList pour compter les occurrences
        for (Long value : arrayList) {
            if (!occurrenceMap.containsKey(value))
                occurrenceMap.put(value, 1);
            else
                occurrenceMap.put(value, occurrenceMap.get(value) + 1);
        }

        return occurrenceMap;
    }

    @Override
    public Key nextKey() {
        return  new CollidingKey(
                Thread.currentThread().getId(),
                random.get().nextInt(Integer.MAX_VALUE),
                list.get(random.get().nextInt(bound)%list.size()),
                bound
        );
    }

    private void fill(int nbHash, double alpha){

        double SCALE = 1.0;
        double SHAPE = alpha;
        int numValues = nbHash;

//        List<Double> doubleValues = new ArrayList<>();

        RandomGenerator rand = RandomGeneratorFactory.createRandomGenerator(new Random(94));
        ParetoDistribution distribution = new ParetoDistribution(rand,SCALE,SHAPE);

//        double maxGeneratedValue = 0;
        for (int i = 0; i < numValues; i++) {
            double randomValue = distribution.sample();
            list.add(Math.round(randomValue));
//            doubleValues.add(randomValue);
//            if (randomValue > maxGeneratedValue) {
//                maxGeneratedValue = randomValue;
//            }
        }

//        double scaleFactor = numValues / maxGeneratedValue;
//
//        for (int i = 0; i < numValues; i++) {
//            double scaledValue = doubleValues.get(i) * scaleFactor;
//            list.add(Math.round(scaledValue));
//        }
    }

    private void fill(){

        double SCALE = 100000.0;
        double SHAPE = 10;
        int numValues = bound;

        RandomGenerator rand = RandomGeneratorFactory.createRandomGenerator(new Random(94));
        ParetoDistribution distribution = new ParetoDistribution(rand,SCALE,SHAPE);

        for (int i = 0; i < numValues; i++) {
            double randomValue = distribution.sample();
            list.add(Math.round(randomValue));
        }

    }

    private static class CollidingKey extends SimpleKey implements Comparable<SimpleKey> {

        private long hash;

        CollidingKey(long tid, long id, long hash, int max_hashes_per_thread) {
            super(tid, id);
            this.hash = hash;
        }

        @Override
        public int hashCode() {
            return (int) hash;
        }

        public String toString() {
            return "("+id+","+hash+")";
        }
    }
}
