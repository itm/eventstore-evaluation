package de.uniluebeck.itm.tr.eventstore.eval;

import com.google.common.base.Function;
import de.uniluebeck.itm.eventstore.EventStore;
import de.uniluebeck.itm.eventstore.EventStoreFactory;
import net.openhft.chronicle.tools.ChronicleTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

public class RunHelper {

    private static final Logger log = LoggerFactory.getLogger(RunHelper.class);

    public static <T> EventStore<T> createEventStore(Class<? extends T> clazz,
                                                     Function<? extends T, byte[]> serializer,
                                                     Function<byte[], ? extends T> deserializer) {
        try {

            Path dir = Files.createTempDirectory("EventStoreEvaluation");
            String basePath = dir.toAbsolutePath().toString();
            ChronicleTools.deleteOnExit(basePath);

            //noinspection unchecked
            return EventStoreFactory.<T>create()
                    .eventStoreWithBasePath(basePath)
                    .setSerializer(clazz, serializer)
                    .setDeserializer(clazz, deserializer)
                    .build();

        } catch (Exception e) {
            log.error("Exception while creating EventStore: ", e);
            throw new RuntimeException(e);
        }
    }
}
