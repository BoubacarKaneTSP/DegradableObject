package eu.cloudbutton.dobj.types;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class CollisionKey /*implements Comparable<CollisionKey>*/{

     String value;

    public CollisionKey(String value){
        this.value = value;
    }

    @Override
    public int hashCode() {
        return 1;
    }
/*
    @Override
    public int compareTo(@NotNull CollisionKey o) {
        return this.value.hashCode() - o.hashCode();
    }*/
}
