package de.uniluebeck.itm.tr.eventstore.eval;

import com.google.common.base.Function;
import org.joda.time.DateTime;

import java.util.Random;

public class RandomLogStringIterator implements Generator<String> {


    private final Random random = new Random(System.currentTimeMillis());
    private final int minPayloadLength;
    private final int maxPayloadLength;

    public RandomLogStringIterator(final int minPayloadLength, final int maxPayloadLength) {
        this.minPayloadLength = minPayloadLength;
        this.maxPayloadLength = maxPayloadLength;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public String next() {


        byte[] b = new byte[random.nextInt(maxPayloadLength - minPayloadLength) + minPayloadLength];
        random.nextBytes(b);

        //noinspection StringBufferReplaceableByString
        return new StringBuffer("urn:wisebed:uzl1:0x")
                .append(Integer.toHexString(random.nextInt()))  // Node ID
                .append(',').append(DateTime.now().getMillis()) // Timestamp
                .append(',').append(random.nextLong())          // Event ID
                .append(',').append(random.nextInt(99))         // Message Type
                .append(',').append(b).toString();
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
