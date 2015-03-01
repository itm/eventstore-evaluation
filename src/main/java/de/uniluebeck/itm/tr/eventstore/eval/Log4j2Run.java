package de.uniluebeck.itm.tr.eventstore.eval;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.AbstractService;
import de.uniluebeck.itm.util.scheduler.SchedulerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.DefaultConfiguration;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newLinkedList;

public class Log4j2Run<T> extends AbstractService implements Run<T> {

    private final long writeAmount;
    private final int writerCount;
    private final Iterator<T> generator;
    private final RunStatsImpl<T> stats;
    private final Class<T> clazz;
    private final SchedulerService executor;
    private static int runNr = 0;

    public Log4j2Run(final SchedulerService executor,
                     final long writeAmount,
                     final int writerCount,
                     final Iterator<T> generator, final Class<T> clazz) {
        this.clazz = clazz;

        if (writeAmount <= 0) {
            throw new IllegalArgumentException("At least one item should be persisted!");
        }

        this.executor = checkNotNull(executor);
        this.writeAmount = writeAmount;
        this.writerCount = writerCount;

        this.generator = checkNotNull(generator);
        this.stats = new RunStatsImpl<>(clazz, runNr++, 0, writerCount);
    }


    @Override
    public RunStats getStats() {
        return stats;
    }


    @Override
    protected void doStart() {

        int threadCount = writerCount;
        final Semaphore semaphore = new Semaphore(threadCount);

        try {

            for (int i = 0; i < writerCount; i++) {

                Logger store = LogManager.getLogger("logger-" + runNr);
                CompletableFuture<Stopwatch> future = new CompletableFuture<>();
                future.thenAccept((stopwatch) -> stats.addWritten(writeAmount, stopwatch)).thenRun(semaphore::release);
                semaphore.acquire();
                executor.execute(createWriter(store, future));
            }

            notifyStarted();

        } catch (InterruptedException e) {
            notifyFailed(e);
        }

        try {
            semaphore.acquire(threadCount);
            notifyStopped();
        } catch (Exception e) {
            notifyFailed(e);
        }
    }


    private Runnable createWriter(Logger store, CompletableFuture<Stopwatch> future) {
        return () -> {

            final LinkedList<T> data = newLinkedList();
            for (long i = 0; i < writeAmount; i++) {
                data.add(generator.next());
            }
            Stopwatch stopwatch = Stopwatch.createStarted();

           for(T d : data) {
                store.info("{}", d);
            }

            stopwatch.stop();
            future.complete(stopwatch);
        };
    }

    @Override
    protected void doStop() {
        // should have happened in doStart()
    }
}
