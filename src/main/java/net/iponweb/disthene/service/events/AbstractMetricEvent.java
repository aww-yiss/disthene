package net.iponweb.disthene.service.events;

import net.iponweb.disthene.bean.Metric;

/**
 * @author Andrei Ivanov
 */
public abstract class AbstractMetricEvent {

    private Metric metric;

    public AbstractMetricEvent(Metric metric) {
        this.metric = metric;
    }

    public Metric getMetric() {
        return metric;
    }
}