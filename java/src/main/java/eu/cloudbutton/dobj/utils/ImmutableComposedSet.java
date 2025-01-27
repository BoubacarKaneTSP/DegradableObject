package eu.cloudbutton.dobj.utils;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ImmutableComposedSet<K> implements Set<K>, Iterable<K> {

    private final List<Set<K>> sets;

    public ImmutableComposedSet(List<Set<K>> sets) {
        this.sets = sets;
    }

    @Override
    public int size() {
        return sets.stream().mapToInt(Set::size).sum();
    }

    @Override
    public boolean isEmpty() {
        return sets.stream().allMatch(Set::isEmpty);
    }

    @Override
    public boolean contains(Object o) {
        return sets.stream().anyMatch(set -> set.contains(o));
    }

    @Override
    public Iterator<K> iterator() {
        Collection<Iterator<K>> collection = new ArrayList<>();
        sets.forEach(set -> collection.add(set.iterator()));
        return new ComposedIterator<>(collection);
    }

    @Override
    public Object[] toArray() {
        throw new RuntimeException();
    }

    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        throw new RuntimeException();
    }

    @Override
    public boolean add(K k) {
        throw new RuntimeException();
    }

    @Override
    public boolean remove(Object o) {
        throw new RuntimeException();
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        throw new RuntimeException();
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends K> c) {
        throw new RuntimeException();
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new RuntimeException();
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new RuntimeException();
    }

    @Override
    public void clear() {
        throw new RuntimeException();
    }

    @Override
    public boolean equals(Object o) {
        throw new RuntimeException();
    }

    @Override
    public int hashCode() {
        return sets.stream().mapToInt(Set::hashCode).sum();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        Iterator<K> iterator = iterator();
        while(iterator.hasNext()) {
            builder.append(iterator.next());
            if(iterator.hasNext()) {
                builder.append(", ");
            }
        }
        builder.append("]");
        return builder.toString();
    }
}
