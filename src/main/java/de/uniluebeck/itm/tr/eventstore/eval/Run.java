package de.uniluebeck.itm.tr.eventstore.eval;

import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.AbstractService;
import de.uniluebeck.itm.eventstore.CloseableIterator;
import de.uniluebeck.itm.eventstore.EventContainer;
import de.uniluebeck.itm.eventstore.EventStore;
import de.uniluebeck.itm.util.scheduler.SchedulerService;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newLinkedList;

/**
 * Each run uses the {@code generator} to generate {@code writeAmount} items to be persisted <emph>per writer</emph> and/or
 * read again from the event store.
 *
 * @param <T> the type of items that will be persisted in and read from the event store
 */
public class Run<T> extends AbstractService {

    private final SchedulerService executor;

    private final long readAmount;
    private final long writeAmount;
    private final int readerCount;
    private final int writerCount;

    private final Class<T> clazz;
    private final Iterator<T> generator;
    private final Function<T, byte[]> serializer;

    private final Function<byte[], T> deserializer;
    private final RunStatsImpl<T> stats;

    private static int runNr = 0;

    /**
     * Creates a new Run.
     *
     * @param executor     the thread pool from which reader and writer threads will be taken
     * @param readAmount   the number of reads to be performed
     * @param writeAmount  the number of items to be generated and persisted
     * @param readerCount  the number of threads for reading messages
     * @param writerCount  the number of threads for writing messages (in separate stores)
     * @param clazz        the class that will be (de)serialized
     * @param generator    the generator function that is used to create items to be persisted
     * @param serializer   used to serialize items of type T to byte[]
     * @param deserializer used to deserialize byte[] to items of type T
     */
    public Run(final SchedulerService executor,
               final long readAmount,
               final long writeAmount,
               final int readerCount,
               final int writerCount,
               final Class<T> clazz,
               final Iterator<T> generator,
               final Function<T, byte[]> serializer,
               final Function<byte[], T> deserializer) {

        if (readerCount == 0 && writerCount == 0) {
            throw new IllegalArgumentException("It doesn't make sense to neither read nor write!");
        }

        if (writeAmount <= 0) {
            throw new IllegalArgumentException("At least one item should be persisted!");
        }

        this.executor = checkNotNull(executor);
        this.writeAmount = writeAmount;
        this.readAmount = readAmount;
        this.readerCount = readerCount;
        this.writerCount = writerCount;

        this.clazz = checkNotNull(clazz);
        this.generator = checkNotNull(generator);
        this.serializer = checkNotNull(serializer);
        this.deserializer = checkNotNull(deserializer);

        this.stats = new RunStatsImpl<>(clazz, runNr++, readerCount, writerCount);
    }

    @Override
    protected void doStart() {

        int threadCount = readerCount + writerCount;
        final Semaphore semaphore = new Semaphore(threadCount);
        final List<EventStore<T>> stores = newLinkedList();
        final Random random = new Random();

        try {

            for (int i = 0; i < writerCount; i++) {

                EventStore<T> store = RunHelper.createEventStore(clazz, serializer, deserializer);
                stores.add(store);
                CompletableFuture<Stopwatch> future = new CompletableFuture<>();
                future.thenAccept((stopwatch) -> stats.addWritten(writeAmount, stopwatch)).thenRun(semaphore::release);
                semaphore.acquire();
                executor.execute(createWriter(store, future));
            }

            for (int i = 0; i < readerCount; i++) {

                CompletableFuture<Stopwatch> future = new CompletableFuture<>();
                future.thenAccept((stopwatch) -> stats.addRead(readAmount, stopwatch)).thenRun(semaphore::release);
                EventStore<T> randomStore = stores.get(random.nextInt(stores.size()));
                semaphore.acquire();
                executor.execute(createReader(randomStore, future));
            }

            notifyStarted();

        } catch (InterruptedException e) {
            notifyFailed(e);
        }

        try {
            semaphore.acquire(threadCount);
            stores.forEach(s -> {
                try {
                    s.close();
                } catch (Exception e) {
                    notifyFailed(e);
                }
            });
            notifyStopped();
        } catch (Exception e) {
            notifyFailed(e);
        }
    }

    private Runnable createReader(EventStore<T> store, CompletableFuture<Stopwatch> future) {
        return () -> {

            Stopwatch stopwatch = Stopwatch.createStarted();

            int dummyHash = 0;
            for (long read = 0; read < readAmount; ) {

                try {

                    CloseableIterator<EventContainer<T>> iterator = store.getAllEvents();
                    while (read < readAmount && iterator.hasNext()) {
                        read++;
                        dummyHash |= iterator.next().getEvent().hashCode();
                    }
                    iterator.close();

                } catch (IOException e) {
                    future.completeExceptionally(e);
                    return;
                }
            }

            dummyHash = dummyHash | 0b01001;

            stopwatch.stop();
            future.complete(stopwatch);
        };
    }

    private Runnable createWriter(EventStore<T> store, CompletableFuture<Stopwatch> future) {
        return () -> {

            Stopwatch stopwatch = Stopwatch.createStarted();

            try {

                for (int i = 0; i < writeAmount; i++) {
                    store.storeEvent(generator.next());
                }

            } catch (IOException e) {
                future.completeExceptionally(e);
                return;
            }

            stopwatch.stop();
            future.complete(stopwatch);
        };
    }

    @Override
    protected void doStop() {
        // this run will stop itself after having successfully executed all there is to do
    }

    public RunStats getStats() {
        if (state() != State.TERMINATED) {
            throw new IllegalArgumentException("Stats can only be calculated after the run has executed!");
        }
        return stats;
    }
}
