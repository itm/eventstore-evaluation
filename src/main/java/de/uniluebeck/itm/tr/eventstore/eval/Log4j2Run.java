package de.uniluebeck.itm.tr.eventstore.eval;

import com.google.common.base.Stopwatch;
import de.uniluebeck.itm.tr.eventstore.eval.generators.Generator;
import de.uniluebeck.itm.util.scheduler.SchedulerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;

public class Log4j2Run<T> extends AbstractRun<T> {

    public Log4j2Run(int runNr, SchedulerService executor, Generator<T> generator, Params params) {
        super(runNr, executor, params, generator);
    }

    @Override
    public RunStats getStats() {
        return stats;
    }

    @Override
    protected Object createStore() {
        return LogManager.getLogger("logger-" + runNr);
    }

    @Override
    protected Runnable createReader(Object store, CompletableFuture<Stopwatch> future) {
        throw new RuntimeException("Log4j2 storage does not provide writing capabilities!");
    }

    @Override
    protected Runnable createWriter(Object store, CompletableFuture<Stopwatch> future) {
        return () -> {

            Logger logger = (Logger) store;
            Stopwatch stopwatch = Stopwatch.createStarted();

            for (int i = 0; i < params.getWritesPerThread(); i++) {
                logger.info("{}", generator.next());
            }

            stopwatch.stop();
            future.complete(stopwatch);
        };
    }
}
