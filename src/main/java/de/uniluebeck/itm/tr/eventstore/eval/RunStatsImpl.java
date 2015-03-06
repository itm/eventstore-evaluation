package de.uniluebeck.itm.tr.eventstore.eval;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import de.uniluebeck.itm.tr.eventstore.eval.generators.Generator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Lists.newLinkedList;

class RunStatsImpl<T> implements RunStats<T> {

    private static class Measurement {
        long amount;

        @Override
        public String toString() {
            return "Measurement{" + "amount=" + amount + ", stopwatch=" + stopwatch + '}';
        }

        Stopwatch stopwatch;

        public Measurement(long amount, Stopwatch stopwatch) {
            this.amount = amount;
            this.stopwatch = stopwatch;
        }
    }

    private final Params params;
    private final int runNr;
    private final Generator<T> generator;

    private final List<Measurement> reads = newLinkedList();
    private final List<Measurement> writes = newLinkedList();

    public RunStatsImpl(int runNr, Params params, Generator<T> generator) {
        this.params = params;
        this.runNr = runNr;
        this.generator = generator;
    }

    @Override
    public Class<T> getItemClass() {
        return generator.getGeneratedClass();
    }

    @Override
    public Duration getTotalWriteDuration() {
        return sumUpDuration(writes);
    }

    @Override
    public Duration getTotalReadDuration() {
        synchronized (reads) {
            return sumUpDuration(reads);
        }
    }

    @Override
    public Duration getAvgDurationForWrites() {
        return calculateAverageDurationPerItemOperation(writes);
    }

    @Override
    public Duration getAvgDurationForReads() {
        return calculateAverageDurationPerItemOperation(reads);
    }

    @Override
    public double getAvgWritingOpsPer(ChronoUnit unit) {
        return calculateAverageDurationPerUnitOverAllMeasurements(unit, writes);
    }

    @Override
    public double getAvgReadingOpsPer(ChronoUnit unit) {
        synchronized (reads) {
            return calculateAverageDurationPerUnitOverAllMeasurements(unit, reads);
        }
    }

    @Override
    public BigInteger getReadAmountTotal() {
        synchronized (reads) {
            return sumUpAmounts(reads);
        }
    }

    @Override
    public BigInteger getWriteAmountTotal() {
        return sumUpAmounts(writes);
    }

    @Override
    public int getReaderCount() {
        return params.getReaderThreadCnt();
    }

    @Override
    public int getWriterCount() {
        return params.getWriterThreadCnt();
    }

    void addWritten(long amount, Stopwatch stopwatch) {
        writes.add(new Measurement(amount, stopwatch));
    }

    void addRead(long amount, Stopwatch stopwatch) {
        synchronized (reads) {
            reads.add(new Measurement(amount, stopwatch));
        }
    }

    private BigInteger sumUpAmounts(List<Measurement> measurements) {
        return measurements.stream()
                .map(m -> BigInteger.valueOf(m.amount))
                .reduce(BigInteger.ZERO, BigInteger::add);
    }

    private Duration sumUpDuration(List<Measurement> measurements) {
        return measurements.stream()
                .map(m -> Duration.ofNanos(m.stopwatch.elapsed(TimeUnit.NANOSECONDS)))
                .reduce(Duration.ZERO, Duration::plus);
    }

    private double calculateAverageDurationPerUnitOverAllMeasurements(ChronoUnit unit,
                                                                      List<Measurement> measurements) {
        MovingAverage movingAverage = new MovingAverage(measurements.size());
        try {
            measurements.forEach(m -> {
                BigDecimal divisor = BigDecimal.valueOf(m.stopwatch.elapsed(TimeUnit.NANOSECONDS));
                movingAverage.add((double) m.amount / divisor.longValueExact());
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("measurements.size() == " + measurements.size());
            measurements.forEach(System.out::println);
        }
        return movingAverage.getAverage() * unit.getDuration().toNanos();
    }

    private Duration calculateAverageDurationPerItemOperation(List<Measurement> measurements) {
        BigInteger totalDurationNanos = BigInteger.valueOf(sumUpDuration(measurements).toNanos());
        BigInteger totalReads = sumUpAmounts(measurements);
        if (totalReads.equals(BigInteger.ZERO)) {
            return Duration.ZERO;
        }
        return Duration.ofNanos(totalDurationNanos.divide(totalReads).longValueExact());
    }

    @Override
    public String toString() {
        String s = "";
        s += "================================== STATS FOR RUN " + runNr + " ==================================\n";
        s += "Item class                      = " + generator.getGeneratedClass() + "\n";
        s += "Reader count                    = " + getReaderCount() + "\n";
        s += "Writer count                    = " + getWriterCount() + "\n";
        s += "Total read  duration            = " + getTotalReadDuration().toMillis() + " ms\n";
        s += "Total write duration            = " + getTotalWriteDuration().toMillis() + " ms\n";
        s += "Total reading ops               = " + getReadAmountTotal() + "\n";
        s += "Total writing ops               = " + getWriteAmountTotal() + "\n";
        s += "Average duration per reading op = " + getAvgDurationForReads().toNanos() + " ns\n";
        s += "Average duration per writing op = " + getAvgDurationForWrites().toNanos() + " ns\n";
        s += "Average reading ops per second  = " + getAvgReadingOpsPer(ChronoUnit.SECONDS) + "\n";
        s += "Average writing ops per second  = " + getAvgWritingOpsPer(ChronoUnit.SECONDS) + "\n";
        s += "\n";
        return s;
    }

    public String toCsv() {
        return Joiner.on(",").join(generator.getGeneratedClass().getCanonicalName(),
                getReaderCount(),
                getWriterCount(),
                getTotalReadDuration().toMillis(),
                getTotalWriteDuration().toMillis(),
                getReadAmountTotal(),
                getWriteAmountTotal(),
                getAvgDurationForReads().toNanos(),
                getAvgDurationForWrites().toNanos(),
                getAvgReadingOpsPer(ChronoUnit.SECONDS),
                getAvgWritingOpsPer(ChronoUnit.SECONDS));
    }

    public static String csvHeader() {
        return "Class, Reader Count, Writer Count, Total Read Duration, Total Write Duration, Total Reading Ops, Total Writing Ops, AVG Duration Per Reading Op, AVG Duration Per Writing Op, AVG Reading Ops Per Second, AVG Writing Ops Per Second";
    }
}
