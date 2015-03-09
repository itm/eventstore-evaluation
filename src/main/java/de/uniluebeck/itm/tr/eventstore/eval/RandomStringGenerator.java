package de.uniluebeck.itm.tr.eventstore.eval;

import com.google.common.base.Function;

import javax.inject.Inject;
import java.util.Random;

public class RandomStringGenerator implements Generator<String> {

    private final int minLength;
    private final int maxLength;

    private Random random = new Random(System.currentTimeMillis());

    @Inject
    public RandomStringGenerator(Params params) {
        this.minLength = params.getPayloadMinLength();
        this.maxLength = params.getPayloadMaxLength();
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public String next() {
        byte[] b = new byte[random.nextInt(maxLength - minLength) + minLength];
        random.nextBytes(b);
        return new String(b);
    }

    @Override
    public Class<String> getGeneratedClass() {
        return String.class;
    }

    @Override
    public Function<byte[], String> getDeserializer() {
        return String::valueOf;
    }

    @Override
    public Function<String, byte[]> getSerializer() {
        return String::getBytes;
    }
}
