package eu.cloudbutton.dobj.map;

public interface CollisionKey extends Comparable<CollisionKey> {

    void setHash(int hash);

    int getHash();
}
