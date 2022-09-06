package eu.cloudbutton.dobj.map;

import lombok.*;

@Data
public class PowerLawCollisionKey extends AbstractCollisionKey implements CollisionKey /*implements Comparable<CollisionKey>*/{

    @Override
    public int hashCode() {
        return getHash();
    }
}
