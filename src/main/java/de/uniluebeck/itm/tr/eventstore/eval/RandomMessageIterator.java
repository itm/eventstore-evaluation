package de.uniluebeck.itm.tr.eventstore.eval;

import com.google.protobuf.ByteString;
import de.uniluebeck.itm.tr.iwsn.messages.DevicesAttachedEvent;
import de.uniluebeck.itm.tr.iwsn.messages.Event;
import de.uniluebeck.itm.tr.iwsn.messages.Message;
import de.uniluebeck.itm.tr.iwsn.messages.UpstreamMessageEvent;
import org.joda.time.DateTime;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Random;

public class RandomMessageIterator implements Iterator<Message> {


    private final Random random = new Random(System.currentTimeMillis());
    private final int minPayloadLength;
    private final int maxPayloadLength;

    public RandomMessageIterator(final int minPayloadLength, final int maxPayloadLength) {
        this.minPayloadLength = minPayloadLength;
        this.maxPayloadLength = maxPayloadLength;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Message next() {

        Message.Builder builder = Message.newBuilder();
        Event.Builder event;

                byte[] b = new byte[random.nextInt(maxPayloadLength-minPayloadLength)+minPayloadLength];
                random.nextBytes(b);
                UpstreamMessageEvent.Builder ume = UpstreamMessageEvent.newBuilder()
                        .setMessageBytes(ByteString.copyFrom(b))
                        .setSourceNodeUrn("urn:wisebed:uzl1:0x" + Integer.toHexString(random.nextInt()))
                        .setTimestamp(DateTime.now().getMillis());
                event = Event.newBuilder()
                        .setEventId(random.nextLong())
                        .setType(Event.Type.UPSTREAM_MESSAGE)
                        .setUpstreamMessageEvent(ume);


        return builder.setType(Message.Type.EVENT).setEvent(event).build();
    }
}
