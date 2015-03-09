package de.uniluebeck.itm.tr.eventstore.eval;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.uniluebeck.itm.tr.common.config.ConfigWithLoggingAndProperties;
import de.uniluebeck.itm.util.propconf.PropConfModule;
import de.uniluebeck.itm.util.scheduler.SchedulerService;
import de.uniluebeck.itm.util.scheduler.SchedulerServiceFactory;
import de.uniluebeck.itm.util.scheduler.SchedulerServiceModule;

import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;
import static de.uniluebeck.itm.tr.common.config.ConfigHelper.parseOrExit;
import static de.uniluebeck.itm.tr.common.config.ConfigHelper.printHelpAndExit;
import static de.uniluebeck.itm.util.propconf.PropConfBuilder.printDocumentationAndExit;

public class Evaluation {

    public static void main(String[] args) {

        Thread.currentThread().setName("Evaluation-Main-Thread");

        // parse command line options
        ConfigWithLoggingAndProperties config = parseOrExit(new ConfigWithLoggingAndProperties(), Evaluation.class, args);

        if (config.helpConfig) {
            printDocumentationAndExit(System.out, Params.class);
        }

        if (config.config == null) {
            printHelpAndExit(config, Evaluation.class);
        }

        // parse evaluation parameters from config file
        Injector injector = Guice.createInjector(
                new PropConfModule(config.config, Params.class),
                new SchedulerServiceModule()
        );

        Params params = injector.getInstance(Params.class);

        // create thread pool that will execute the various writer and reader threads
        SchedulerService executor = injector.getInstance(SchedulerServiceFactory.class).create(-1, "EvaluationExecutor");
        executor.startAsync().awaitRunning();

        // instantiate generator that produces items to be persisted
        Generator<?> generator = injector.getInstance(params.getGeneratorClass());

        // warm up phase, results will be dismissed
        if (params.getWarmUp()) {
            System.out.println("Executing warm up phase...");
            executeRun(executor, params, generator, -1);
        }

        System.out.println("Warm up done, executing runs. This could take a while...");

        // execute a run
        List<RunStats> stats = runEvaluation(executor, params, generator);

        System.out.println(RunStatsImpl.csvHeader());

        stats.stream().map(RunStats::toCsv).forEach(System.out::println);

        executor.stopAsync().awaitTerminated();
        System.out.println("Finished");

        System.exit(0);
    }

    private static <T> List<RunStats> runEvaluation(SchedulerService executor, Params params, Generator<T> generator) {

        final List<RunStats> stats = newLinkedList();

        for (int runNr = 1; runNr <= params.getRuns(); runNr++) {

            stats.add(executeRun(executor, params, generator, runNr));
            gcIfConfigured(params, runNr);

        }

        return stats;
    }

    private static <T> RunStats<T> executeRun(SchedulerService executor, Params params, Generator<T> generator, int runNr) {

        Run<T> run = new EventStoreRun<>(runNr, executor, params, generator);

        run.startAsync();
        run.awaitTerminated();

        return run.getStats();
    }

    private static void gcIfConfigured(Params params, int runNr) {
        if (params.getGcBetweenRuns()) {
            System.out.println("Running garbage collection after run " + runNr + "...");
            try {
                System.gc();
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}
