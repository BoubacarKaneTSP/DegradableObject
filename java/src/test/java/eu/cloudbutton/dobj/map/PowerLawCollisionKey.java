package eu.cloudbutton.dobj.map;

import lombok.*;

@Data
public class PowerLawCollisionKey extends AbstractCollisionKey /*implements Comparable<CollisionKey>*/{

    @Override
    public int hashCode() {
        return getHash();
    }
}
