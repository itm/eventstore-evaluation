package de.uniluebeck.itm.tridentcom.eval;

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

    private final Random random = new Random();

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Message next() {
        Message.Builder builder = Message.newBuilder();
        double rand = random.nextDouble();
        Event.Builder event = null;
        if (rand < 0.02) {
            DevicesAttachedEvent.Builder dae = DevicesAttachedEvent
                    .newBuilder()
                    .addNodeUrns("urn:wisebed:uzl1:0x" + Integer.toHexString(random.nextInt()))
                    .setTimestamp(DateTime.now().getMillis());
            event = Event.newBuilder().setDevicesAttachedEvent(dae);
        } else {
            try {
                String payload = "abcdefghijklmnopfjalskdkrwueriosdfiocxvkljxkjfaln,mcxnv,nmcv,ymxnca,msdnfaklsjc";
                UpstreamMessageEvent.Builder ume = UpstreamMessageEvent.newBuilder()
                        .setMessageBytes(ByteString.copyFrom(payload, "UTF-8"))
                        .setSourceNodeUrn("urn:wisebed:uzl1:0x" + Integer.toHexString(random.nextInt()))
                        .setTimestamp(DateTime.now().getMillis());
                event = Event.newBuilder().setUpstreamMessageEvent(ume);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
        builder.setEvent(event);
        return builder.build();
    }
}
