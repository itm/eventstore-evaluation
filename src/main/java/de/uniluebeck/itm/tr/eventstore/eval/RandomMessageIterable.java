package de.uniluebeck.itm.tr.eventstore.eval;

import de.uniluebeck.itm.tr.iwsn.messages.Message;

import java.util.Iterator;

public class RandomMessageIterable implements Iterable<Message> {
    @Override
    public Iterator<Message> iterator() {
        return new RandomMessageIterator();
    }
}
