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

    private final RandomStringIterator stringIterator;

    private final Random random = new Random();

    public RandomMessageIterator(final int minPayloadLength, final int maxPayloadLength) {
        stringIterator = new RandomStringIterator(minPayloadLength, maxPayloadLength);
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Message next() {

        Message.Builder builder = Message.newBuilder();
        double rand = random.nextDouble();
        Event.Builder event;

            try {

                String payload = "abcdefghijklmnopfjalskdkrwueriosdfiocxvkljxkjfaln,mcxnv,nmcv,ymxnca,msdnfaklsjc";
                UpstreamMessageEvent.Builder ume = UpstreamMessageEvent.newBuilder()
                        .setMessageBytes(ByteString.copyFrom(payload, "UTF-8"))
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
}