package eu.cloudbutton.dobj.types;

import lombok.Data;
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
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j <= i; j++) {
                listHashCode.add(i);
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
