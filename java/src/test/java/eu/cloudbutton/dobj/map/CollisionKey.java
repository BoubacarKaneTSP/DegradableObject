package eu.cloudbutton.dobj.map;

import lombok.Data;
import nl.peterbloem.powerlaws.DiscreteApproximate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Data
public class CollisionKey /*implements Comparable<CollisionKey>*/{

     String value;
     List<Integer> listHashCode = new ArrayList<>();
     protected final ThreadLocalRandom random;



    public CollisionKey(String value){
        this.value = value;
        this.random = ThreadLocalRandom.current();
        int bound = 1000;

        List<Integer> data = new DiscreteApproximate(1, 1.7).generate(bound);

        int i = 0;

        double ratio = 100000 / 175000000.0; //10⁵ is ~ the number of follow max on twitter and 175_000_000 is the number of user on twitter (stats from the article)
        long max = (long) ((long) 100000 * ratio);

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
                listHashCode.add(j);
            }
        }
    }

    @Override
    public int hashCode() {
        return listHashCode.get(random.nextInt(listHashCode.size()));
    }
/*
    @Override
    public int compareTo(@NotNull CollisionKey o) {
        return this.value.hashCode() - o.hashCode();
    }*/
}
