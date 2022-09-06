package eu.cloudbutton.dobj.map;

public class AbstractCollisionKey implements CollisionKey {

    private int hash;

    @Override
    public void setHash(int hash){
        this.hash = hash;
    }

    @Override
    public int getHash(){
        return hash;
    }
}
