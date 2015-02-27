package de.uniluebeck.itm.tr.eventstore.eval;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.Random;

public class RandomBigIntegerIterator implements Iterator<BigInteger> {

    private Random random = new Random();

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public BigInteger next() {
        return BigInteger.valueOf(random.nextLong());
    }
}
