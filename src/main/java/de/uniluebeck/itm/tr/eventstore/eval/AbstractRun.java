package de.uniluebeck.itm.tr.eventstore.eval;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.AbstractService;
import de.uniluebeck.itm.util.scheduler.SchedulerService;

import java.io.Closeable;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;

/**
 * Each run uses the {@code generator} to generate {@code writeAmount} items to be persisted <emph>per writer</emph> and/or
 * read again from the event store.
 *
 * @param <T> the type of items that will be persisted in and read from the event store
 */
public abstract class AbstractRun<T> extends AbstractService implements Run<T> {

    protected final SchedulerService executor;
    protected final Params params;
    protected final Generator<T> generator;
    protected final RunStatsImpl<T> stats;
    protected final int runNr;

    /**
     * Creates a new Run.
     *
     * @param executor  the thread pool from which reader and writer threads will be taken
     * @param params    the parameters for this run
     * @param generator the generator function that is used to create items to be persisted
     */
    public AbstractRun(int runNr, SchedulerService executor, Params params, Generator<T> generator) {

        if (params.getReaderThreadCnt() == 0 && params.getWriterThreadCnt() == 0) {
            throw new IllegalArgumentException("It doesn't make sense to neither read nor write!");
        }

        if (params.getWriterThreadCnt() <= 0 && params.getWritesPerThread() <= 0) {
            throw new IllegalArgumentException("At least one item should be persisted!");
        }

        this.runNr = runNr;
        this.executor = checkNotNull(executor);
        this.params = checkNotNull(params);
        this.generator = checkNotNull(generator);

        this.stats = new RunStatsImpl<>(runNr, params, generator);
    }

    @Override
    protected void doStart() {

        int threadCount = params.getWriterThreadCnt() + params.getReaderThreadCnt();

        final Semaphore semaphore = new Semaphore(threadCount);
        final Map<Integer, Object> stores = newHashMap();
        final Map<Integer, Runnable> writers = newHashMap();
        final Map<Integer, Runnable> readers = newHashMap();
        final Random random = new Random();

        try {

            System.out.println("Creating " + params.getWriterThreadCnt() + " stores and writer threads...");

            // create a store for each writer and the writer before starting individual writers
            for (int i = 1; i <= params.getWriterThreadCnt(); i++) {

                Object store = createStore();
                stores.put(i, store);

                CompletableFuture<Stopwatch> future = new CompletableFuture<>();
                future.thenAccept((stopwatch) -> stats.addWritten(params.getWritesPerThread(), stopwatch))
                        .thenRun(semaphore::release);
                Runnable writer = createWriter(store, future);
                writers.put(i, writer);
                semaphore.acquire();
            }

            System.out.println("Creating " + params.getReaderThreadCnt() + " reader threads...");

            // create readers
            for (int i = 0; i < params.getReaderThreadCnt(); i++) {

                CompletableFuture<Stopwatch> future = new CompletableFuture<>();
                future.thenAccept((stopwatch) -> stats.addRead(params.getReadsPerThread(), stopwatch))
                        .thenRun(semaphore::release);
                Object randomStore = stores.get(random.nextInt(stores.size()));
                readers.put(i, createReader(randomStore, future));
            }

            System.out.println("Starting " + params.getWriterThreadCnt() + " writers and " + params.getReaderThreadCnt() + " readers...");

            // start writers
            writers.forEach((nr, writer) -> {
                try {
                    semaphore.acquire();
                    executor.execute(writer);
                } catch (InterruptedException e) {
                    System.err.println("InterruptedException while acquiring semaphore for writer!");
                    e.printStackTrace();
                    System.exit(1);
                }
            });

            // start readers
            readers.forEach((nr, reader) -> {
                try {
                    semaphore.acquire();
                    executor.execute(reader);
                } catch (InterruptedException e) {
                    System.err.println("InterruptedException while acquiring semaphore for reader!");
                    e.printStackTrace();
                    System.exit(1);
                }
            });

            notifyStarted();

        } catch (InterruptedException e) {
            notifyFailed(e);
        }

        try {

            semaphore.acquire(threadCount);

            System.out.println("All " + params.getWriterThreadCnt() + " writers and " + params.getReaderThreadCnt()
                    + " readers have completed this run nr " + runNr + "!");

            stores.forEach((idx, s) -> {
                if (s instanceof Closeable) {
                    try {
                        System.out.println("Closing store " + idx + " of " + stores.size());
                        ((Closeable) s).close();
                    } catch (Exception e) {
                        notifyFailed(e);
                    }
                }
            });

            notifyStopped();

        } catch (Exception e) {
            notifyFailed(e);
        }
    }

    @Override
    protected void doStop() {
        // this run will stop itself after having successfully executed all there is to do
    }

    @Override
    public RunStats getStats() {
        if (state() != State.TERMINATED) {
            throw new IllegalArgumentException("Stats can only be calculated after the run has executed!");
        }
        return stats;
    }

    protected abstract Object createStore();

    protected abstract Runnable createReader(Object store, CompletableFuture<Stopwatch> future);

    protected abstract Runnable createWriter(Object store, CompletableFuture<Stopwatch> future);
}
