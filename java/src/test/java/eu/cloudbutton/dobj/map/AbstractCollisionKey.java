package eu.cloudbutton.dobj.map;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractCollisionKey implements CollisionKey{

    private int hash;

    @Override
    public void setHash(int hash){
        this.hash = hash;
    }

    @Override
    public int getHash(){
        return hash;
    }

    @Override
    public int compareTo(@NotNull CollisionKey collisionKey) {
        int x = getHash();
        int y = collisionKey.getHash();
        if (x > y) {
            return 1;
        } else if (x < y) {
            return -1;
        }
        return 0;
    }

}
