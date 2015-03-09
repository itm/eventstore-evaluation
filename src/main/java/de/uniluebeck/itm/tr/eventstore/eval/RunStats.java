package de.uniluebeck.itm.tr.eventstore.eval;

import com.google.common.collect.Iterators;
import com.google.common.collect.Table;

import java.math.BigInteger;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public interface RunStats<T> {

    public static enum Field {

        CLASS,
        READER_THREAD_COUNT,
        WRITER_THREAD_COUNT,
        TOTAL_READ_DURATION,
        TOTAL_WRITE_DURATION,
        TOTAL_READING_OPS,
        TOTAL_WRITING_OPS,
        AVG_DURATION_PER_READING_OP_NS,
        AVG_DURATION_PER_WRITING_OP_NS,
        AVG_READING_OPS_PER_S,
        AVG_WRITING_OPS_PER_S;

        public static String[] names() {
            Field[] fields = values();
            String[] names = new String[fields.length];
            for (int i = 0; i < fields.length; i++) {
                names[i] = fields[i].name();
            }
            return names;
        }
    }

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

    /**
     * Creates a CSV (comma-separated value) String of the values
     *
     * @return a CSV String
     * @see
     */
    public String toCsv();

    /**
     * Returns an array containing the headers for CSV output
     *
     * @return an array with CSV headers
     */
    public String[] csvHeaders();

    /**
     * Returns an array containing a CSV representation of this runs stats.
     *
     * @return an array with CSV values
     */
    public String[] csvValues();

    /**
     * Returns a String containing the header row for CSV output
     *
     * @return CSV header row
     */
    public String toCsvHeaders();

    /**
     * Returns the column names of the table representation (in a human-friendly sorting).
     *
     * @return tables column names
     * @see RunStats#asTable(java.util.List)
     */
    default public String[] tableCols() {
        return Field.names();
    }

    default public String get(Field field) {
        switch (field) {
            case CLASS:
                return getItemClass().getCanonicalName();
            case READER_THREAD_COUNT:
                return String.valueOf(getReaderCount());
            case WRITER_THREAD_COUNT:
                return String.valueOf(getWriterCount());
            case TOTAL_READ_DURATION:
                return getTotalReadDuration().toString();
            case TOTAL_WRITE_DURATION:
                return getTotalWriteDuration().toString();
            case TOTAL_READING_OPS:
                return getReadAmountTotal().toString();
            case TOTAL_WRITING_OPS:
                return getWriteAmountTotal().toString();
            case AVG_DURATION_PER_READING_OP_NS:
                return String.valueOf(getAvgDurationForReads().toNanos());
            case AVG_DURATION_PER_WRITING_OP_NS:
                return String.valueOf(getAvgDurationForWrites().toNanos());
            case AVG_READING_OPS_PER_S:
                return String.valueOf(getAvgReadingOpsPer(ChronoUnit.SECONDS));
            case AVG_WRITING_OPS_PER_S:
                return String.valueOf(getAvgWritingOpsPer(ChronoUnit.SECONDS));
            default:
                throw new RuntimeException("Missing case branch for RunStats.Fields enum");
        }
    }
}
