package de.uniluebeck.itm.tr.eventstore.eval;

import com.google.common.base.Function;

import java.math.BigInteger;
import java.util.Random;

public class RandomBigIntegerGenerator implements Generator<BigInteger> {

    private Random random = new Random();

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public BigInteger next() {
        return BigInteger.valueOf(random.nextLong());
    }

    @Override
    public Class<BigInteger> getGeneratedClass() {
        return BigInteger.class;
    }

    @Override
    public Function<byte[], BigInteger> getDeserializer() {
        return BigInteger::new;
    }

    @Override
    public Function<BigInteger, byte[]> getSerializer() {
        return BigInteger::toByteArray;
    }
}
