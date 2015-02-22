package de.uniluebeck.itm.tridentcom.eval;

import com.google.common.base.Function;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.protobuf.InvalidProtocolBufferException;
import de.uniluebeck.itm.tr.iwsn.messages.Message;
import de.uniluebeck.itm.util.scheduler.SchedulerService;
import de.uniluebeck.itm.util.scheduler.SchedulerServiceFactory;
import de.uniluebeck.itm.util.scheduler.SchedulerServiceModule;

import java.math.BigInteger;
import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;

public class Evaluation {


    public static final Function<byte[], String> STRING_DESERIALIZER = String::new;

    public static final com.google.common.base.Function<byte[], BigInteger> BIGINT_DESERIALIZER = BigInteger::new;

    public static final com.google.common.base.Function<String, byte[]> STRING_SERIALIZER = String::getBytes;

    public static final com.google.common.base.Function<BigInteger, byte[]> BIGINT_SERIALIZER = BigInteger::toByteArray;

    public static final Function<Message, byte[]> MESSAGE_SERIALIZER = Message::toByteArray;

    public static final Function<byte[], Message> MESSAGE_DESERIALIZER = (byte[] data) -> {
        try {
            return Message.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            return null;
        }
    };

    public static void main(String[] args) {

        Injector injector = Guice.createInjector(new SchedulerServiceModule());
        SchedulerService executor = injector.getInstance(SchedulerServiceFactory.class).create(-1, "EvaluationExecutor");

        List<RunStats> stats = newLinkedList();

        final long writeAmount = 1000000;
        final long readAmount = 1000000;

        for (int readerCount = 0; readerCount <= 10; readerCount++) {
            for (int writerCount = 0; writerCount <= 10; writerCount++) {

                RandomMessageIterator generator = new RandomMessageIterator();

                Run<Message> run = new Run<>(
                        executor, readAmount, writeAmount, readerCount, writerCount,
                        Message.class, generator, MESSAGE_SERIALIZER, MESSAGE_DESERIALIZER
                );

                run.startAsync();
                run.awaitTerminated();

                stats.add(run.getStats());

                System.out.println(stats);
            }
        }


    }
}
