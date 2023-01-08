package eu.cloudbutton.dobj.key;

import nl.peterbloem.powerlaws.DiscreteApproximate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RetwisKeyGenerator implements KeyGenerator {


    private final List<Long> list;
    private ThreadLocal<Random> random;
    protected final int bound;

    public RetwisKeyGenerator(int max_hashes_per_thread) {
        System.out.println("HAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
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

    private void fill(){

        List<Integer> data = new DiscreteApproximate(1, 1.39).generate(bound);
        int i = 0;

        int nbUsers = 1000000;
        double ratio = 100000 / 175000000.0; //10⁵ is ~ the number of follow max on twitter and 175_000_000 is the number of user on twitter (stats from the article)
        long max = (long) ((long) nbUsers * ratio);

        System.out.println();
        System.out.println();
        System.out.println(data);
        System.out.println();
        System.out.println();
        for (int val: data){
            if (val >= max) {
                data.set(i, (int) max);
            }
            if (val < 0)
                data.set(i, 0);
            i++;
        }

        for (int j = 0; j < bound; j++) {
            for (int k = 0; k < data.get(j); k++) {
                long toAdd = j;
                list.add(toAdd);
            }
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
