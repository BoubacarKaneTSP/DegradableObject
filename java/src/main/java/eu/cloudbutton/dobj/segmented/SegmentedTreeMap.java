package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.swsr.SWSRTreeMap;
import eu.cloudbutton.dobj.utils.BaseSegmentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class SegmentedTreeMap<K,V> extends SegmentedMap<SWSRTreeMap,K,V> implements Map<K,V> {

    public SegmentedTreeMap() {
        super(SWSRTreeMap.class);
    }

}
