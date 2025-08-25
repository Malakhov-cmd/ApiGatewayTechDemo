package ru.tech.demo.service;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.CompositeHealth;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class HealthMetricsPublisherService {
    private static final Logger log = LoggerFactory.getLogger(HealthMetricsPublisherService.class);

    private final MeterRegistry meterRegistry;
    private final HealthEndpoint healthEndpoint;

    private ScheduledExecutorService executor;

    public HealthMetricsPublisherService(MeterRegistry meterRegistry, HealthEndpoint healthEndpoint) {
        this.meterRegistry = meterRegistry;
        this.healthEndpoint = healthEndpoint;
    }


    @PostConstruct
    public void startScheduler() {
        executor = Executors.newScheduledThreadPool(2);
        executor.scheduleAtFixedRate(this::publishHealthMetrics, 0, 10, TimeUnit.SECONDS);
    }

    public void publishHealthMetrics() {
        try {
            CompositeHealth health = (CompositeHealth) healthEndpoint.health();
            health.getComponents().forEach((name, component) -> {
                double value = "UP".equals(component.getStatus().getCode()) ? 1.0 : 0.0;

                Gauge.builder("application_health_status", () -> value)
                        .description("Health status of component: " + name)
                        .tag("component", name)
                        .register(meterRegistry);
            });
        } catch (Exception e) {
            log.error("Error while process scheduled health endpoint to Prometheus format. Details: {}", e.toString());
        }
    }

    @PreDestroy
    public void stopScheduler() {
        if (executor != null) {
            executor.shutdown();
        }
    }
}
