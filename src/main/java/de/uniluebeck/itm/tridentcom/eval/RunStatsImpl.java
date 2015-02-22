package de.uniluebeck.itm.tridentcom.eval;

import com.google.common.base.Stopwatch;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Lists.newLinkedList;

class RunStatsImpl implements RunStats {

    private static class Measurement {
        long amount;
        Stopwatch stopwatch;
        public Measurement(long amount, Stopwatch stopwatch) {
            this.amount = amount;
            this.stopwatch = stopwatch;
        }
    }

    private final int readerCount;
    private final int writerCount;

    private List<Measurement> reads = newLinkedList();
    private List<Measurement> writes = newLinkedList();

    private static int runNr = 0;

    public RunStatsImpl(int readerCount, int writerCount) {
        this.readerCount = readerCount;
        this.writerCount = writerCount;
        this.runNr++;
    }

    @Override
    public Duration getTotalWriteDuration() {
        return sumUpDuration(writes);
    }

    @Override
    public Duration getTotalReadDuration() {
        return sumUpDuration(reads);
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
    public BigDecimal getAvgItemsWrittenPer(ChronoUnit unit) {
        return calculateAverageDurationPerUnitOverAllMeasurements(unit, writes);
    }

    @Override
    public BigDecimal getAvgItemsReadPer(ChronoUnit unit) {
        return calculateAverageDurationPerUnitOverAllMeasurements(unit, reads);
    }

    @Override
    public BigInteger getReadAmountTotal() {
        return sumUpAmounts(reads);
    }

    @Override
    public BigInteger getWriteAmountTotal() {
        return sumUpAmounts(writes);
    }

    @Override
    public int getReaderCount() {
        return readerCount;
    }

    @Override
    public int getWriterCount() {
        return writerCount;
    }

    void addWritten(long amount, Stopwatch stopwatch) {
        writes.add(new Measurement(amount, stopwatch));
        long duration = stopwatch.elapsed(TimeUnit.NANOSECONDS);
        long nanosPerItem = stopwatch.elapsed(TimeUnit.NANOSECONDS) / amount;
        double itemsPerSecond = (double) amount / stopwatch.elapsed(TimeUnit.SECONDS);
    }

    void addRead(long amount, Stopwatch stopwatch) {
        reads.add(new Measurement(amount, stopwatch));
        long duration = stopwatch.elapsed(TimeUnit.NANOSECONDS);
        long nanosPerItem = stopwatch.elapsed(TimeUnit.NANOSECONDS) / amount;
        double itemsPerSecond = (double) amount / stopwatch.elapsed(TimeUnit.SECONDS);
    }

    private BigInteger sumUpAmounts(List<Measurement> measurements) {
        BigInteger sum = BigInteger.ZERO;
        measurements.forEach(m -> sum.add(BigInteger.valueOf(m.amount)));
        return sum;
    }

    private Duration sumUpDuration(List<Measurement> measurements) {
        Duration d = Duration.ZERO;
        measurements.forEach(m -> d.plusNanos(m.stopwatch.elapsed(TimeUnit.NANOSECONDS)));
        return d;
    }

    private BigDecimal calculateAverageDurationPerUnitOverAllMeasurements(ChronoUnit unit, List<Measurement> measurements) {
        MovingAverage movingAverage = new MovingAverage(measurements.size());
        measurements.forEach(m -> {
            long durationInUnit = Duration.ofNanos(m.stopwatch.elapsed(TimeUnit.NANOSECONDS)).get(unit);
            movingAverage.add(BigDecimal.valueOf(m.amount).divide(BigDecimal.valueOf(durationInUnit), RoundingMode.HALF_UP));
        });
        return movingAverage.getAverage();
    }

    private Duration calculateAverageDurationPerItemOperation(List<Measurement> measurements) {
        BigInteger totalDurationNanos = BigInteger.valueOf(sumUpDuration(measurements).toNanos());
        BigInteger totalReads = sumUpAmounts(measurements);
        return Duration.ofNanos(totalDurationNanos.divide(totalReads).longValueExact());
    }

    @Override
    public String toString() {
        String s = "";
        s += "================================== STATS FOR RUN " + runNr + " ==================================\n";
        s += "Reader count                    = " + getReaderCount() + "\n";
        s += "Writer count                    = " + getWriterCount() + "\n";
        s += "Total read  duration            = " + getTotalReadDuration() + "\n";
        s += "Total write duration            = " + getTotalWriteDuration() + "\n";
        s += "Total reading ops               = " + getReadAmountTotal() + "\n";
        s += "Total writing ops               = " + getWriteAmountTotal() + "\n";
        s += "Average duration per reading op = " + getAvgDurationForReads() + "\n";
        s += "Average duration per writing op = " + getAvgDurationForWrites() + "\n";
        s += "Average reading ops per second  = " + getAvgItemsReadPer(ChronoUnit.SECONDS) + "\n";
        s += "Average writing ops per second  = " + getAvgItemsWrittenPer(ChronoUnit.SECONDS) + "\n";
        s += "\n";
        return s;
    }
}
