package de.uniluebeck.itm.tr.eventstore.eval;

import com.google.common.util.concurrent.Service;

public interface Run<T> extends Service {
    RunStats<T> getStats();
}
