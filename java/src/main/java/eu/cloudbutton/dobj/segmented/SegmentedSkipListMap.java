package eu.cloudbutton.dobj.segmented;

import eu.cloudbutton.dobj.asymmetric.swmr.map.SWMRSkipListMap;
import eu.cloudbutton.dobj.utils.FactoryIndice;
import eu.cloudbutton.dobj.swsr.SWSRSkipListMap;
import eu.cloudbutton.dobj.utils.BaseSegmentation;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;


public class SegmentedSkipListMap<K,V> extends SegmentedMap<SWMRSkipListMap,K,V> {

    public SegmentedSkipListMap() {
        super(SWMRSkipListMap.class);
    }

}
