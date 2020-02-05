package org.apereo.cas.config;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Copy/pasted from the origin CAS class. Disable EnableMetrics, clean the MetricsRegistry and remove the reporters.
 */
@Configuration("casMetricsConfiguration")
public class CasMetricsConfiguration extends MetricsConfigurerAdapter {

    /**
     * Metric registry metric registry.
     *
     * @return the metric registry
     */
    @RefreshScope
    @Bean
    public MetricRegistry metrics() {
        return new MetricRegistry();
    }

    @Bean
    public HealthCheckRegistry healthCheckMetrics() {
        return new HealthCheckRegistry();
    }

    @Override
    public MetricRegistry getMetricRegistry() {
        return metrics();
    }

    @Override
    public HealthCheckRegistry getHealthCheckRegistry() {
        return healthCheckMetrics();
    }

    @Override
    public void configureReporters(final MetricRegistry metricRegistry) {
    }
}
