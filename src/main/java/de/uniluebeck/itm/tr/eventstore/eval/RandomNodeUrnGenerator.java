package de.uniluebeck.itm.tr.eventstore.eval;

import com.google.common.base.Function;

import javax.inject.Inject;
import java.util.Random;

public class RandomNodeUrnGenerator implements Generator<String> {

    private final Random random;

    @Inject
    @SuppressWarnings("UnusedParameters")
    public RandomNodeUrnGenerator(Params params) {
        random = new Random();
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public String next() {
        return "urn:eval:0x" + Integer.toHexString(random.nextInt());
    }

    @Override
    public Class<String> getGeneratedClass() {
        return String.class;
    }

    @Override
    public Function<byte[], String> getDeserializer() {
        return String::new;
    }

    @Override
    public Function<String, byte[]> getSerializer() {
        return String::getBytes;
    }
}
