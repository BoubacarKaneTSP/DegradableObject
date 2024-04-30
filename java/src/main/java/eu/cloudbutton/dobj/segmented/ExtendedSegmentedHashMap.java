package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.utils.FactoryIndice;
import eu.cloudbutton.dobj.asymmetric.swmr.map.SWMRHashMap;
import eu.cloudbutton.dobj.utils.ExtendedSegmentation;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class ExtendedSegmentedHashMap<K,V> extends ExtendedSegmentedMap<Hashtable,K,V> implements Map<K,V> {

    public ExtendedSegmentedHashMap(FactoryIndice factoryIndice) {
        super(Hashtable.class, factoryIndice);
    }
}
