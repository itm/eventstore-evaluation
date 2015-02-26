package de.uniluebeck.itm.tridentcom.eval;

import com.google.common.base.Function;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.protobuf.InvalidProtocolBufferException;
import de.uniluebeck.itm.tr.iwsn.messages.Message;
import de.uniluebeck.itm.util.logging.LogLevel;
import de.uniluebeck.itm.util.logging.Logging;
import de.uniluebeck.itm.util.scheduler.SchedulerService;
import de.uniluebeck.itm.util.scheduler.SchedulerServiceFactory;
import de.uniluebeck.itm.util.scheduler.SchedulerServiceModule;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;

public class Evaluation {

    static {
        Logging.setLoggingDefaults(LogLevel.TRACE);
    }

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
        executor.startAsync().awaitRunning();


        final long writeAmount = 1000000;
        final long readAmount = 1000000;
        final RandomBigIntegerIterator bigIntGenerator = new RandomBigIntegerIterator();
        final RandomMessageIterator messageGenerator = new RandomMessageIterator();


        final List<RunStats> bigIntStats = runEvaluation(
                executor,
                BigInteger.class, bigIntGenerator,
                readAmount, writeAmount,
                BIGINT_SERIALIZER, BIGINT_DESERIALIZER
        );

        bigIntStats.forEach(System.out::println);
        System.out.println();

        final List<RunStats> messageStats = runEvaluation(
                executor,
                Message.class, messageGenerator,
                readAmount, writeAmount,
                MESSAGE_SERIALIZER, MESSAGE_DESERIALIZER
        );

        messageStats.forEach(System.out::println);
        System.out.println();

        executor.stopAsync().awaitTerminated();
    }

    private static <T> List<RunStats> runEvaluation(SchedulerService executor,
                                                    Class<T> clazz, Iterator<T> generator,
                                                    long readAmount, long writeAmount,
                                                    Function<T, byte[]> serializer, Function<byte[], T> deserializer) {

        final List<RunStats> stats = newLinkedList();
        for (int writerCount = 1; writerCount <= 5; writerCount++) {
            for (int readerCount = 0; readerCount <= 5; readerCount++) {

                Run<T> run = new Run<>(
                        executor, readAmount, writeAmount, readerCount, writerCount,
                        clazz, generator, serializer, deserializer
                );

                run.startAsync();
                run.awaitTerminated();

                stats.add(run.getStats());

            }
        }
        return stats;
    }
}
