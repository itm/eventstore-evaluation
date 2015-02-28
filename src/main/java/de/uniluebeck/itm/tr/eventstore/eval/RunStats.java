package de.uniluebeck.itm.tr.eventstore.eval;

import java.math.BigInteger;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public interface RunStats<T> {

    /**
     * Returns the class of items that were persisted and read.
     *
     * @return the item class
     */
    public Class<T> getItemClass();

    /**
     * Returns the total execution of the writing operations of this run.
     *
     * @return total execution time
     */
    public Duration getTotalWriteDuration();

    /**
     * Returns the total execution of the reading operations of this run.
     *
     * @return total execution time
     */
    public Duration getTotalReadDuration();

    /**
     * Calculates the average duration needed to persist an item, based on the number of items persisted and the total
     * execution time of the run.
     *
     * @return the average duration needed to persist an item.
     */
    public Duration getAvgDurationForWrites();

    /**
     * Calculates the average duration needed to read an item, based on the number of items read and the total execution
     * time of the run.
     *
     * @return the average duration needed to read an item
     */
    public Duration getAvgDurationForReads();

    /**
     * Returns the average number of items that were written per time unit.
     *
     * @param unit the unit, e.g., ms
     * @return the average number of items that were written per time unit
     */
    public double getAvgWritingOpsPer(ChronoUnit unit);

    /**
     * Returns the average number of items that were read per time unit.
     *
     * @param unit the unit, e.g., ms
     * @return the average number of items that were read per time unit
     */
    public double getAvgReadingOpsPer(ChronoUnit unit);

    /**
     * The number of items that have been read in total by all reader threads in this run.
     *
     * @return number of items that have been read
     */
    public BigInteger getReadAmountTotal();

    /**
     * The number of items that have been written in total by all writer threads in this run.
     *
     * @return number of items that have been written
     */
    public BigInteger getWriteAmountTotal();

    /**
     * The number of parallel reader threads that were active during the run.
     *
     * @return number of parallel readers
     */
    public int getReaderCount();

    /**
     * The number of parallel writer threads that were active during the run.
     *
     * @return number of parallel writers
     */
    public int getWriterCount();

    public String toCsv();
}
