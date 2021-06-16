package eu.cloudbutton.dobj.types;

import org.javatuples.Pair;

public interface Cloner <T>{

    Pair<T,Integer> clone(Pair<T,Integer> t);
}
