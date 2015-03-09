package de.uniluebeck.itm.tr.eventstore.eval;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import de.uniluebeck.itm.util.propconf.PropConf;
import de.uniluebeck.itm.util.propconf.converters.ClassTypeConverter;

public class Params {

    @PropConf(
            usage = "number of repeated runs to be executed",
            defaultValue = "10"
    )
    public static final String RUNS = "runs";

    @Inject
    @Named(RUNS)
    private long runs;

    @PropConf(
            usage = "garbage collection between runs",
            defaultValue = "true"
    )
    public static final String GC_BETWEEN_RUNS = "gc_between_runs";

    @Inject
    @Named(GC_BETWEEN_RUNS)
    private boolean gcBetweenRuns;

    @PropConf(
            usage = "number of items to be persisted in the warm up phase to minimize the JIT compilers influence",
            defaultValue = "true"
    )
    public static final String WARM_UP = "warm_up";

    @Inject
    @Named(WARM_UP)
    private boolean warmUp;

    @PropConf(
            usage = "number of items to be persisted per writer thread",
            defaultValue = "1000000"
    )
    public static final String WRITES_PER_THREAD = "writes_per_thread";

    @Inject
    @Named(WRITES_PER_THREAD)
    private long writesPerThread;

    @PropConf(
            usage = "number of items to be read per writer thread",
            defaultValue = "1000000"
    )
    public static final String READS_PER_THREAD = "reads_per_thread";

    @Inject
    @Named(READS_PER_THREAD)
    private long readsPerThread;

    @PropConf(
            usage = "number of reader threads",
            defaultValue = "0"
    )
    public static final String READER_THREAD_CNT = "reader_thread_cnt";

    @Inject
    @Named(READER_THREAD_CNT)
    private int readerThreadCnt = 0;

    @PropConf(
            usage = "number of writer threads",
            defaultValue = "1"
    )
    public static final String WRITER_THREAD_CNT = "writer_thread_cnt";

    @Inject
    @Named(WRITER_THREAD_CNT)
    private int writerThreadCnt = 5;

    @PropConf(
            usage = "minimal number of bytes for payload to be generated",
            defaultValue = "40"
    )
    public static final String PAYLOAD_MIN_LENGTH = "payload_min_length";

    @Inject
    @Named(PAYLOAD_MIN_LENGTH)
    private int payloadMinLength = 40;

    @PropConf(
            usage = "maximum number of bytes for payload to be generated",
            defaultValue = "120"
    )
    public static final String PAYLOAD_MAX_LENGTH = "payload_max_length";

    @Inject
    @Named(PAYLOAD_MAX_LENGTH)
    private int payloadMaxLength;

    @PropConf(
            usage = "fully-qualified class name of the generator function to create items to be persisted",
            defaultValue = "de.uniluebeck.itm.tr.eventstore.eval.RandomMessageGenerator",
            typeConverter = ClassTypeConverter.class
    )
    public static final String GENERATOR_CLASS = "generator_class";

    @Inject
    @Named(GENERATOR_CLASS)
    private Class<? extends Generator<?>> generatorClass;

    public boolean getWarmUp() {
        return warmUp;
    }

    public long getWritesPerThread() {
        return writesPerThread;
    }

    public long getReadsPerThread() {
        return readsPerThread;
    }

    public int getReaderThreadCnt() {
        return readerThreadCnt;
    }

    public int getWriterThreadCnt() {
        return writerThreadCnt;
    }

    public int getPayloadMinLength() {
        return payloadMinLength;
    }

    public int getPayloadMaxLength() {
        return payloadMaxLength;
    }

    public Class<? extends Generator<?>> getGeneratorClass() {
        return generatorClass;
    }

    public long getRuns() {
        return runs;
    }

    public boolean getGcBetweenRuns() {
        return gcBetweenRuns;
    }
}
