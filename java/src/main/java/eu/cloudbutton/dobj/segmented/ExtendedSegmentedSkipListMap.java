package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.asymmetric.swmr.map.SWMRSkipListMap;
import eu.cloudbutton.dobj.utils.ExtendedSegmentation;
import eu.cloudbutton.dobj.utils.FactoryIndice;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ExtendedSegmentedSkipListMap<K,V> extends ExtendedSegmentedMap<SWMRSkipListMap,K,V>  implements Map<K,V> {

    public ExtendedSegmentedSkipListMap(FactoryIndice factoryIndice) {
        super(SWMRSkipListMap.class, factoryIndice);
    }

}
