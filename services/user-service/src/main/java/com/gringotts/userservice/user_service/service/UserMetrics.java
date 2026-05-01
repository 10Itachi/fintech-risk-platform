package com.gringotts.userservice.user_service.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class UserMetrics {

    private final Counter success;
    private final Counter failure;

    public UserMetrics(MeterRegistry registry) {
        this.success = registry.counter("user.create.success");
        this.failure = registry.counter("user.create.failure");
    }

    public void incrementSuccess() {
        success.increment();
    }

    public void incrementFailure() {
        failure.increment();
    }
}
/**
 * CUSTOM OPERATIONAL METRICS (Monitoring & Observability)
 * ------------------------------------------------------
 * PURPOSE:
 * This class acts as a "Scoreboard" for the User Service, capturing real-time
 * data on the success and failure rates of user registration attempts.
 *
 * KEY COMPONENTS:
 * 1. MeterRegistry: The central "Database" for Micrometer. It registers our
 * counters so they can be scraped by external tools like Prometheus.
 * 2. Counter: A specific Micrometer metric type that is "Monotonic" (it only
 * goes up). These are used to track the cumulative count of events.
 *
 * TECHNICAL BENEFITS:
 * - Thread-Safety: The .increment() method is built-in and atomic, meaning it
 * can handle multiple simultaneous sign-ups without losing count.
 * - External Visibility: These metrics are exposed via Spring Boot Actuator,
 * allowing for live dashboards in Grafana and automated alerting.
 * - Performance: Unlike logging, which is IO-heavy, incrementing a counter
 * in memory is extremely fast and has negligible impact on latency.
 */