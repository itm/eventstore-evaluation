package de.uniluebeck.itm.tr.eventstore.eval;

import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import de.uniluebeck.itm.eventstore.CloseableIterator;
import de.uniluebeck.itm.eventstore.EventContainer;
import de.uniluebeck.itm.eventstore.EventStore;
import de.uniluebeck.itm.eventstore.EventStoreFactory;
import de.uniluebeck.itm.util.scheduler.SchedulerService;
import net.openhft.chronicle.tools.ChronicleTools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class EventStoreRun<T> extends AbstractRun<T> {

    public EventStoreRun(int runNr, SchedulerService executor, Params params, Generator<T> generator) {
        super(runNr, executor, params, generator);
    }

    @Override
    protected Object createStore() {
        return createEventStore(
                generator.getGeneratedClass(),
                generator.getSerializer(),
                generator.getDeserializer()
        );
    }

    @Override
    protected Runnable createReader(Object store, CompletableFuture<Stopwatch> future) {
        return () -> {

            @SuppressWarnings("unchecked") EventStore<T> eventStore = (EventStore<T>) store;
            Stopwatch stopwatch = Stopwatch.createStarted();

            long readsPerThread = params.getReadsPerThread();

            for (long read = 0; read < readsPerThread; ) {

                try {

                    CloseableIterator<EventContainer<T>> iterator = eventStore.getAllEvents();
                    while (read < readsPerThread && iterator.hasNext()) {
                        read++;
                    }
                    iterator.close();

                } catch (IOException e) {
                    future.completeExceptionally(e);
                    return;
                }
            }

            stopwatch.stop();
            future.complete(stopwatch);
        };
    }

    @Override
    protected Runnable createWriter(Object store, CompletableFuture<Stopwatch> future) {
        return () -> {

            @SuppressWarnings("unchecked") EventStore<T> eventStore = (EventStore<T>) store;
            Stopwatch stopwatch = Stopwatch.createStarted();

            try {

                for (long i = 0; i < params.getWritesPerThread(); i++) {
                    eventStore.storeEvent(generator.next());
                }

            } catch (IOException e) {
                future.completeExceptionally(e);
                return;
            }

            stopwatch.stop();
            future.complete(stopwatch);
        };
    }

    public static <T> EventStore<T> createEventStore(Class<? extends T> clazz,
                                                     Function<? extends T, byte[]> serializer,
                                                     Function<byte[], ? extends T> deserializer) {
        try {

            Path dir = Files.createTempDirectory("EventStoreEvaluation");
            String basePath = dir.toAbsolutePath().toString();
            ChronicleTools.deleteOnExit(basePath);

            //noinspection unchecked
            return EventStoreFactory.<T>create()
                    .eventStoreWithBasePath(basePath)
                    .setSerializer(clazz, serializer)
                    .setDeserializer(clazz, deserializer)
                    .build();

        } catch (Exception e) {
            System.err.println("Exception while creating EventStore!");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
