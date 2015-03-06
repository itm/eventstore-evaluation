package de.uniluebeck.itm.tr.eventstore.eval;

import com.google.common.base.Stopwatch;
import de.uniluebeck.itm.tr.eventstore.eval.generators.Generator;
import de.uniluebeck.itm.util.scheduler.SchedulerService;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class Log4jRun<T> extends AbstractRun<T> {

    public Log4jRun(int runNr, SchedulerService executor, Params params, Generator<T> generator) {
        super(runNr, executor, params, generator);
    }

    @Override
    protected Object createStore() {
        try {
            File tempFile = File.createTempFile("log4j-run-" + runNr, "");
            tempFile.deleteOnExit();
            RollingFileAppender appender = new RollingFileAppender(new PatternLayout(), tempFile.getAbsolutePath());
            org.apache.log4j.Logger.getRootLogger().addAppender(appender);
            org.apache.log4j.Logger.getRootLogger().setLevel(Level.INFO);
            return org.apache.log4j.LogManager.getLogger("log4j-run-" + runNr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Runnable createReader(Object store, CompletableFuture<Stopwatch> future) {
        throw new RuntimeException("Readers are not supported for Log4j based stores!");
    }

    @Override
    protected Runnable createWriter(Object store, CompletableFuture<Stopwatch> future) {
        return () -> {

            java.util.logging.Logger log = (java.util.logging.Logger) store;
            Stopwatch stopwatch = Stopwatch.createStarted();

            for (int i = 0; i < params.getWritesPerThread(); i++) {
                log.info((String) generator.next());
            }

            stopwatch.stop();
            future.complete(stopwatch);
        };
    }

}
