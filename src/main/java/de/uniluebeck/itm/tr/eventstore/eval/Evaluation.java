package de.uniluebeck.itm.tr.eventstore.eval;

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

import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;

public class Evaluation {

    static {
        Logging.setLoggingDefaults(LogLevel.TRACE);
    }

    public static final Function<byte[], String> STRING_DESERIALIZER = String::new;

    public static final com.google.common.base.Function<String, byte[]> STRING_SERIALIZER = String::getBytes;

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
        final long readAmount = 0;
        final int readers = 0;
        final int writers = 5;
        final int minLength = 40;
        final int maxLength = 120;

        final RandomMessageIterator messageGenerator = new RandomMessageIterator(minLength, maxLength);
        final RandomStringIterator stringIterator = new RandomStringIterator(minLength, maxLength);

        final List<RunStats> trash = runEventStoreEvaluation(executor, String.class, stringIterator, 10000, 10000, STRING_SERIALIZER, STRING_DESERIALIZER, readers, writers);
        final List<RunStats> trash2= runLoggerEvaluation(executor, String.class, stringIterator, 10000, writers);
        final List<RunStats> eventStorestringStats = runEventStoreEvaluation(executor, String.class, stringIterator, readAmount, writeAmount, STRING_SERIALIZER, STRING_DESERIALIZER, readers, writers);

        System.out.println(RunStatsImpl.csvHeader());
        eventStorestringStats.stream().map(RunStats::toCsv).forEach(System.out::println);


        /*final List<RunStats> evenStoremessageStats = runEventStoreEvaluation(
                executor,
                Message.class, messageGenerator,
                readAmount, writeAmount,
                MESSAGE_SERIALIZER, MESSAGE_DESERIALIZER
        );

        evenStoremessageStats.stream().map(RunStats::toCsv).forEach(System.out::println); */

        System.out.println("---- Log4j: ----");

        final List<RunStats> loggerStringStats = runLoggerEvaluation(executor, String.class, stringIterator, writeAmount, readers);
        loggerStringStats.stream().map(RunStats::toCsv).forEach(System.out::println);

        executor.stopAsync().awaitTerminated();
        System.out.println("Finished");
    }

    private static <T> List<RunStats> runEventStoreEvaluation(SchedulerService executor,
                                                              Class<T> clazz, Iterator<T> generator,
                                                              long readAmount, long writeAmount,
                                                              Function<T, byte[]> serializer, Function<byte[], T> deserializer, int maxReaders, int maxWriters) {

        final List<RunStats> stats = newLinkedList();
        for (int writerCount = 1; writerCount <= maxWriters; writerCount++) {
            for (int readerCount = 0; readerCount <= maxReaders; readerCount++) {

                Run<T> run = new EventStoreRun<>(
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

    private static <T> List<RunStats> runLoggerEvaluation(SchedulerService executor, Class<T> clazz, Iterator<T> generator, long writeAmount, int maxWriters) {
        final List<RunStats> stats = newLinkedList();
        for (int writerCount = 1; writerCount <= maxWriters; writerCount++) {

                Run<T> run = new LoggerRun<>(executor,
                        writeAmount, writerCount, generator, clazz
                );

                run.startAsync();
                run.awaitTerminated();

                stats.add(run.getStats());

        }
        return stats;
    }

}
