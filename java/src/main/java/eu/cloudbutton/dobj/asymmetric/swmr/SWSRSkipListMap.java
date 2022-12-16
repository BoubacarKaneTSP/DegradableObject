package eu.cloudbutton.dobj.asymmetric.swmr;

import eu.cloudbutton.dobj.utils.NonLinearizable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentNavigableMap;

public class SWSRSkipListMap <K extends Comparable<K>, V> implements ConcurrentNavigableMap<K, V> {

    private static final sun.misc.Unsafe UNSAFE;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    // The default probability to use when selecting a random level.
    private static final double DEFAULT_ITERATION_PROBABILITY = 0.2;
    // An instance of the random number generator.
    private final ThreadLocal<Random> random;
    // The probability with which to continue iterating while selecting a level.
    private final double iterationProbability;
    // The head of the list.
    private Node<K, V> head;
    // The size of the list.
    private int size;

    /**
     * Creates a new skip list with default parameters.
     */
    public SWSRSkipListMap() {
        this(DEFAULT_ITERATION_PROBABILITY);
    }

    /**
     * Creates a new skip list with the specified iteration probability.
     *
     * @param iterationProbability The probability with which to continue iterating during level selection.
     */
    public SWSRSkipListMap(double iterationProbability) {
        random = ThreadLocal.withInitial(() -> {return new Random(System.nanoTime());});
        this.iterationProbability = iterationProbability;
        clear();
    }

    @Override
    @NonLinearizable
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        Node<K, V> cur = getLowestHead().next;
        while (cur != null) {
            boolean match = Objects.equals(value, cur.value);
            if (match) {
                return true;
            }

            cur = cur.next;
        }

        return false;
    }

    @Override
    public V get(Object key) {
        @SuppressWarnings("unchecked")
        K k = (K) key;
        if (k == null) {
            throw new NullPointerException();
        }

        Node<K, V> cur = head;
        while (cur != null) {
            while (cur.isNextKeyLessThan(k)) {
                cur = cur.next;
            }

            if (cur.isNextKeyEqualTo(k)) {
                return cur.next.value;
            }

            cur = cur.down;
        }

        return null;
    }

    @Override
    public V put(K key, V value) {
        if (key == null) {
            throw new NullPointerException();
        }

        long level = getRandomLevel();
        if (level > head.level) {
            head = new Node<>(null, null, level, null, head);
        }

        Node<K, V> cur = head;
        Node<K, V> prevLevelEntry = null;

        while (cur != null) {
            while (cur.isNextKeyLessThan(key)) {
                cur = cur.next;
            }

            // If a node with the key already exists in the list, update the value
            if (cur.isNextKeyEqualTo(key)) {
                V prevValue = cur.next.value;
                cur.next.value = value;
                UNSAFE.fullFence();
                return prevValue;
            }

            // Move down if we are not at or beneath the selected level.
            if (level < cur.level) {
                cur = cur.down;
                continue;
            }

            // Insert a new node in the list.
            Node<K, V> n = new Node<>(key, value, cur.level, cur.next, null);
            if (prevLevelEntry != null) {
                prevLevelEntry.down = n;
            }

            prevLevelEntry = n;
            cur.next = n;
            UNSAFE.fullFence();
            cur = cur.down;
        }

        size++;
        return null;
    }

    @Override
    public V remove(Object key) {
        @SuppressWarnings("unchecked")
        K k = (K) key;
        if (k == null) {
            throw new NullPointerException();
        }

        Node<K, V> cur = head;
        boolean found = false;
        V value = null;

        while (cur != null) {
            while (cur.isNextKeyLessThan(k)) {
                cur = cur.next;
            }

            if (cur.isNextKeyEqualTo(k)) {
                found = true;
                value = cur.next.value;
                cur.next = cur.next.next;
                UNSAFE.fullFence();
            }

            cur = cur.down;
        }

        if (found) {
            size--;
        }

        return value;
    }

    @Override
    @NonLinearizable
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        head = new Node<>(null, null, 0, null, null);
        UNSAFE.fullFence();
        size = 0;
    }

    @Override
    public ConcurrentNavigableMap<K, V> subMap(K k, boolean b, K k1, boolean b1) {
        return null;
    }

    @Override
    public ConcurrentNavigableMap<K, V> headMap(K k, boolean b) {
        return null;
    }

    @Override
    public ConcurrentNavigableMap<K, V> tailMap(K k, boolean b) {
        return null;
    }

    @Override
    public Comparator<? super K> comparator() {
        return null;
    }

    @Override
    public ConcurrentNavigableMap<K, V> subMap(K k, K k1) {
        return null;
    }

    @Override
    public ConcurrentNavigableMap<K, V> headMap(K k) {
        return null;
    }

    @Override
    public ConcurrentNavigableMap<K, V> tailMap(K k) {
        return null;
    }

    @Override
    public K firstKey() {
        return null;
    }

    @Override
    public K lastKey() {
        return null;
    }

    @Override
    public Entry<K, V> lowerEntry(K k) {
        return null;
    }

    @Override
    public K lowerKey(K k) {
        return null;
    }

    @Override
    public Entry<K, V> floorEntry(K k) {
        return null;
    }

    @Override
    public K floorKey(K k) {
        return null;
    }

    @Override
    public Entry<K, V> ceilingEntry(K k) {
        return null;
    }

    @Override
    public K ceilingKey(K k) {
        return null;
    }

    @Override
    public Entry<K, V> higherEntry(K k) {
        return null;
    }

    @Override
    public K higherKey(K k) {
        return null;
    }

    @Override
    public Entry<K, V> firstEntry() {
        return null;
    }

    @Override
    public Entry<K, V> lastEntry() {
        return null;
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
        return null;
    }

    @Override
    public Entry<K, V> pollLastEntry() {
        return null;
    }

    @Override
    public ConcurrentNavigableMap<K, V> descendingMap() {
        return null;
    }

    @Override
    public NavigableSet<K> navigableKeySet() {
        return keySet();
    }

    @Override
    public NavigableSet<K> keySet() {
        return new KeySet(this);
    }


    private Iterator<K> keyIterator() {
        return new Iterator<K>() {
            Node<K, V> cur = getLowestHead().next;
            @Override
            public boolean hasNext() {
                return cur!=null;
            }

            @Override
            public K next() {
                K k = cur.key;
                cur = cur.next;
                return k;
            }
        };
    }

    @Override
    public NavigableSet<K> descendingKeySet() {
        return null;
    }

    @Override
    public Collection<V> values() {
        Collection<V> result = new HashSet<>(size);
        Node<K, V> cur = getLowestHead().next;
        while (cur != null) {
            result.add(cur.value);
            cur = cur.next;
        }

        return result;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> result = new HashSet<>(size);
        Node<K, V> cur = getLowestHead().next;
        while (cur != null) {
            result.add(new AbstractMap.SimpleEntry<>(cur.key, cur.value));
            cur = cur.next;
        }

        return result;
    }

    // Selects a random level by incrementing a counter a random number of times.
    private long getRandomLevel() {
        long level = 0;
        while (level <= size && random.get().nextDouble() < iterationProbability) {
            level++;
        }

        return level;
    }

    // Gets the head node at the lowest level in the list.
    private Node<K, V> getLowestHead() {
        Node<K, V> cur = head;
        while (cur.down != null) {
            cur = cur.down;
        }

        return cur;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (!(o instanceof SWSRSkipListMap)) {
            return false;
        }

        SWSRSkipListMap<?, ?> other = (SWSRSkipListMap<?, ?>) o;
        return entrySet().equals(other.entrySet());
    }

    @Override
    public V putIfAbsent(@NotNull K k, V v) {
        return null;
    }

    @Override
    public boolean remove(@NotNull Object o, Object o1) {
        return false;
    }

    @Override
    public boolean replace(@NotNull K k, @NotNull V v, @NotNull V v1) {
        return false;
    }

    @Override
    public V replace(@NotNull K k, @NotNull V v) {
        return null;
    }

    private static class Node<K extends Comparable<K>, V> {
        public K key;
        public V value;
        public long level;
        public Node<K, V> next;
        public Node<K, V> down;

        public Node(K key, V value, long level, Node<K, V> next, Node<K, V> down) {
            this.key = key;
            this.value = value;
            this.level = level;
            this.next = next;
            this.down = down;
        }

        public boolean isNextKeyLessThan(K key) {
            return (next != null && next.key.compareTo(key) < 0);
        }

        public boolean isNextKeyEqualTo(K key) {
            return (next != null && next.key.equals(key));
        }
    }

    static final class KeySet<K extends Comparable<K>>
            extends AbstractSet<K> implements NavigableSet<K> {
        private final SWSRSkipListMap<K,Object> m;
        KeySet(SWSRSkipListMap<K,Object> map) { m = map; }
        public int size() { return m.size(); }
        public boolean isEmpty() { return m.isEmpty(); }
        public boolean contains(Object o) { return m.containsKey(o); }
        public boolean remove(Object o) { return m.remove(o) != null; }
        public void clear() { m.clear(); }
        public K lower(K e) { return m.lowerKey(e); }
        public K floor(K e) { return m.floorKey(e); }
        public K ceiling(K e) { return m.ceilingKey(e); }
        public K higher(K e) { return m.higherKey(e); }

        @Nullable
        @Override
        public K pollFirst() {
            return null;
        }

        @Nullable
        @Override
        public K pollLast() {
            return null;
        }

        public Comparator<? super K> comparator() { return m.comparator(); }
        public K first() { return m.firstKey(); }
        public K last() { return m.lastKey(); }
        public Iterator<K> iterator() {
            if (m instanceof SWSRSkipListMap)
                return ((SWSRSkipListMap<K,Object>)m).keyIterator();
            return null;
        }

        @NotNull
        @Override
        public NavigableSet<K> descendingSet() {
            return null;
        }

        @NotNull
        @Override
        public Iterator<K> descendingIterator() {
            return null;
        }

        @NotNull
        @Override
        public NavigableSet<K> subSet(K k, boolean b, K e1, boolean b1) {
            return null;
        }

        @NotNull
        @Override
        public NavigableSet<K> headSet(K k, boolean b) {
            return null;
        }

        @NotNull
        @Override
        public NavigableSet<K> tailSet(K k, boolean b) {
            return null;
        }

        @NotNull
        @Override
        public SortedSet<K> subSet(K k, K e1) {
            return null;
        }

        @NotNull
        @Override
        public SortedSet<K> headSet(K k) {
            return null;
        }

        @NotNull
        @Override
        public SortedSet<K> tailSet(K k) {
            return null;
        }

    }

}
