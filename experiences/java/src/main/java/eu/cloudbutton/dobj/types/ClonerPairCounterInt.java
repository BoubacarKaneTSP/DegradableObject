package eu.cloudbutton.dobj.types;

import org.javatuples.Pair;

public class ClonerPairCounterInt<T> implements Cloner<T>{

    @Override
    public Pair<T, Integer> clone(Pair<T, Integer> t) {
        return (Pair<T, Integer>) new Pair<>(new Counter((Counter) t.getValue0()), Integer.valueOf(t.getValue1()));
    }
}
