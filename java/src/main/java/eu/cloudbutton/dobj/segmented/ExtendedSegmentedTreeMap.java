package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.asymmetric.swmr.map.SWMRTreeMap;
import eu.cloudbutton.dobj.utils.ExtendedSegmentation;
import eu.cloudbutton.dobj.utils.FactoryIndice;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ExtendedSegmentedTreeMap<K,V> extends ExtendedSegmentedMap<HashMap,K,V>  implements Map<K,V> {

    public ExtendedSegmentedTreeMap(FactoryIndice factoryIndice) {
        super(HashMap.class, factoryIndice);
    }

}
