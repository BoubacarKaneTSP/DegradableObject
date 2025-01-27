package eu.cloudbutton.dobj.utils;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ImmutableComposedCollection<K> implements Collection<K>, Iterable<K> {

    private final List<Collection<K>> collections;

    public ImmutableComposedCollection(List<Collection<K>> sets) {
        this.collections = sets;
    }

    @Override
    public int size() {
        return collections.stream().mapToInt(Collection::size).sum();
    }

    @Override
    public boolean isEmpty() {
        return collections.stream().allMatch(Collection::isEmpty);
    }

    @Override
    public boolean contains(Object o) {
        return collections.stream().anyMatch(set -> set.contains(o));
    }

    @Override
    public Iterator<K> iterator() {
        Collection<Iterator<K>> collection = new ArrayList<>();
        collections.forEach(set -> collection.add(set.iterator()));
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
        return collections.stream().mapToInt(Collection::hashCode).sum();
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
