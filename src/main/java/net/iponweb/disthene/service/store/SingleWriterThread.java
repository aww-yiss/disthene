package net.iponweb.disthene.service.store;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import net.iponweb.disthene.bean.Metric;
import net.iponweb.disthene.bus.DistheneBus;
import net.iponweb.disthene.events.StoreErrorEvent;
import net.iponweb.disthene.events.StoreSuccessEvent;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.Executor;

/**
 * @author Andrei Ivanov
 */
public class SingleWriterThread extends WriterThread {
    private Logger logger = Logger.getLogger(SingleWriterThread.class);

    public SingleWriterThread(String name, DistheneBus bus, Session session, PreparedStatement statement, Queue<Metric> metrics, Executor executor) {
        super(name, bus, session, statement, metrics, executor);
    }

    @Override
    public void run() {
        while (!shutdown) {
            Metric metric = metrics.poll();
            if (metric != null) {
                store(metric);
            }
        }
    }

    private void store(Metric metric) {
        ResultSetFuture future = session.executeAsync(statement.bind(
                metric.getRollup() * metric.getPeriod(),
                Collections.singletonList(metric.getValue()),
                metric.getTenant(),
                metric.getRollup(),
                metric.getPeriod(),
                metric.getPath(),
                metric.getUnixTimestamp()
        ));

        Futures.addCallback(future,
                new FutureCallback<ResultSet>() {
                    @Override
                    public void onSuccess(ResultSet result) {
                        bus.post(new StoreSuccessEvent(1));
                    }

                    @SuppressWarnings("NullableProblems")
                    @Override
                    public void onFailure(Throwable t) {
                        bus.post(new StoreErrorEvent(1));
                        logger.error(t);
                    }
                },
                executor
        );
    }

}
