package de.uniluebeck.itm.tr.eventstore.eval;

import com.google.common.base.Function;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.uniluebeck.itm.tr.iwsn.messages.Event;
import de.uniluebeck.itm.tr.iwsn.messages.Message;
import de.uniluebeck.itm.tr.iwsn.messages.UpstreamMessageEvent;
import org.joda.time.DateTime;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.util.Random;

public class RandomMessageGenerator implements Generator<Message> {

    public static final Function<Message, byte[]> MESSAGE_SERIALIZER = Message::toByteArray;

    public static final Function<byte[], Message> MESSAGE_DESERIALIZER = (byte[] data) -> {
        try {
            return Message.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            return null;
        }
    };

    private final RandomStringGenerator payloadGenerator;

    private final Random random = new Random();

    @Inject
    public RandomMessageGenerator(Params params) {
        payloadGenerator = new RandomStringGenerator(params);
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Message next() {

        Message.Builder builder = Message.newBuilder();
        Event.Builder event;

        try {

            UpstreamMessageEvent.Builder ume = UpstreamMessageEvent.newBuilder()
                    .setMessageBytes(ByteString.copyFrom(payloadGenerator.next(), "UTF-8"))
                    .setSourceNodeUrn("urn:wisebed:uzl1:0x" + Integer.toHexString(random.nextInt()))
                    .setTimestamp(DateTime.now().getMillis());

            event = Event.newBuilder()
                    .setEventId(random.nextLong())
                    .setType(Event.Type.UPSTREAM_MESSAGE)
                    .setUpstreamMessageEvent(ume);

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return builder.setType(Message.Type.EVENT).setEvent(event).build();
    }

    @Override
    public Class<Message> getGeneratedClass() {
        return Message.class;
    }

    @Override
    public Function<byte[], Message> getDeserializer() {
        return MESSAGE_DESERIALIZER;
    }

    @Override
    public Function<Message, byte[]> getSerializer() {
        return MESSAGE_SERIALIZER;
    }
}
