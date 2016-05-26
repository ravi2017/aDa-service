package fk.sp.aDa.module;

import com.google.inject.AbstractModule;

import com.codahale.metrics.MetricRegistry;
import com.palominolabs.metrics.guice.MetricsInstrumentationModule;

public class MetricsModule extends AbstractModule {

    private MetricRegistry metricRegistry;

    public MetricsModule(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void configure() {
        install(new MetricsInstrumentationModule(metricRegistry));
    }
}
