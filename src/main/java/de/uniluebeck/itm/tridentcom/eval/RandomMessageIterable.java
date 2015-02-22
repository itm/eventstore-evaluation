package de.uniluebeck.itm.tridentcom.eval;

import de.uniluebeck.itm.tr.iwsn.messages.Message;

import java.util.Iterator;

public class RandomMessageIterable implements Iterable<Message> {
    @Override
    public Iterator<Message> iterator() {
        return new RandomMessageIterator();
    }
}
