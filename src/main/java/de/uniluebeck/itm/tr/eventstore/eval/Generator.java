package de.uniluebeck.itm.tr.eventstore.eval;

import com.google.common.base.Function;

import java.util.Iterator;

public interface Generator<T> extends Iterator<T> {

    Class<T> getGeneratedClass();

    Function<byte[], T> getDeserializer();

    Function<T, byte[]> getSerializer();

}
