package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.asymmetric.swmr.map.SWMRHashMap;
import eu.cloudbutton.dobj.utils.BaseSegmentation;
import eu.cloudbutton.dobj.utils.ImmutableComposedCollection;
import eu.cloudbutton.dobj.utils.ImmutableComposedSet;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SegmentedHashMap<K,V> extends SegmentedMap<SWMRHashMap,K,V> {

    public SegmentedHashMap() {
        super(SWMRHashMap.class);
    }

}
