package de.uniluebeck.itm.tr.eventstore.eval;

import java.util.Iterator;
import java.util.Random;

public class RandomStringIterator implements Iterator<String>{
    private final int minLength;
    private final int maxLength;
    private Random random = new Random(System.currentTimeMillis());

    public RandomStringIterator(final int minLength, final int maxLength) {

        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public String next() {
        final byte[] b = new byte[random.nextInt(maxLength-minLength)+minLength];
        random.nextBytes(b);
        return new String(b);
    }
}
