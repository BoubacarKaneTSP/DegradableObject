package eu.cloudbutton.dobj.key;

import nl.peterbloem.powerlaws.DiscreteApproximate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RetwisKeyGenerator implements KeyGenerator {

    private final int MAX_KEYS = 1000;

    private final List<Long> list;
    private final ThreadLocalRandom random;
    private final int bound;

    public RetwisKeyGenerator() {
        this.random = ThreadLocalRandom.current();
        this.bound = MAX_KEYS;
        this.list =  new ArrayList<>();
        fill();
    }

    @Override
    public long nextKey() {
        return list.get(random.nextInt(bound));
    }

    private void fill(){

        List<Integer> data = new DiscreteApproximate(1, 1.39).generate(bound);
        int i = 0;

        int nbUsers = 1000000;
        double ratio = 100000 / 175000000.0; //10⁵ is ~ the number of follow max on twitter and 175_000_000 is the number of user on twitter (stats from the article)
        long max = (long) ((long) nbUsers * ratio);

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
}
