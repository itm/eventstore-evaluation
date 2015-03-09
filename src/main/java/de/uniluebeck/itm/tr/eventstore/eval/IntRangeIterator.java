package de.uniluebeck.itm.tr.eventstore.eval;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class IntRangeIterator implements Iterator<Integer> {

    private final int maxExclusive;

    private int current;

    protected IntRangeIterator(int minInclusive, int maxExclusive) {
        if (minInclusive >= maxExclusive) {
            throw new IllegalArgumentException("minInclusive must be < maxExclusive");
        }
        this.current = minInclusive;
        this.maxExclusive = maxExclusive;
    }

    public boolean hasNext() {
        return current < maxExclusive;
    }

    public Integer next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return current++;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public static Iterator<Integer> create(int minInclusive, int maxExclusive) {
        return new IntRangeIterator(minInclusive, maxExclusive);
    }

    public static Iterable<Integer> createIterable(int minInclusive, int maxExclusive) {
        return () -> new IntRangeIterator(minInclusive, maxExclusive);
    }
}