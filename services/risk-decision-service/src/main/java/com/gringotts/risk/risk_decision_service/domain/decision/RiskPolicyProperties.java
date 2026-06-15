package com.gringotts.risk.risk_decision_service.domain.decision;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@EnableConfigurationProperties(RiskPolicyProperties.class)
@ConfigurationProperties(prefix = "risk.policy")
@Getter
@Setter
public class RiskPolicyProperties {

    // EXISTING FIELDS
    private BigDecimal maxSingleAmount;
    private BigDecimal maxDailyAmount;
    private Set<String> blockedCountries;
    private BigDecimal cardWithdrawalLimit;
    private Duration maxFutureDrift;
    private Duration maxPastDrift;

    // final thresholds
    private double declineThreshold;
    private double reviewThreshold;

    // DEPRECATED (keep for backward compatibility)
   /* private int softDeclineScore;
    private int softReviewScore;*/

    // policy metadata
    private String version;
    private Instant activatedAt;

    // NEW → SOFT RULE CONFIG
    private SoftRuleProperties softRules;

    // VALIDATION
    @PostConstruct
    public void validate() {

        // ML THRESHOLDS
        if (declineThreshold < 0 || declineThreshold > 1) {
            throw new IllegalStateException("declineThreshold must be between 0 and 1");
        }

        if (reviewThreshold < 0 || reviewThreshold > 1) {
            throw new IllegalStateException("reviewThreshold must be between 0 and 1");
        }

        if (declineThreshold < reviewThreshold) {
            throw new IllegalStateException("declineThreshold must be >= reviewThreshold");
        }

        /* // SOFT SCORE (DEPRECATED)
        if (softDeclineScore < 0 || softReviewScore < 0) {
            throw new IllegalStateException("Soft score thresholds must be non-negative");
        }

        if (softDeclineScore < softReviewScore) {
            throw new IllegalStateException("softDeclineScore must be >= softReviewScore");
        }*/

        // POLICY METADATA
        if (version == null || version.isBlank()) {
            throw new IllegalStateException("Policy version must be defined");
        }

        if (activatedAt == null) {
            throw new IllegalStateException("Policy activation time must be defined");
        }

        // NEW → SOFT RULE VALIDATION
        if (softRules == null) {
            throw new IllegalStateException("softRules configuration must be defined");
        }

        softRules.validate();
    }


    // NEW → INNER CLASS
    @Getter
    @Setter
    public static class SoftRuleProperties {

        private double highAmount;
        private double mediumAmount;

        private int velocityHigh;
        private int velocityMedium;

        private double cardHighAmount;

        private Delta deltas;

        public void validate() {

            if (highAmount <= 0 || mediumAmount <= 0) {
                throw new IllegalStateException("Soft rule amount thresholds must be positive");
            }

            if (velocityHigh <= 0 || velocityMedium <= 0) {
                throw new IllegalStateException("Velocity thresholds must be positive");
            }

            if (velocityHigh < velocityMedium) {
                throw new IllegalStateException("velocityHigh must be >= velocityMedium");
            }

            if (deltas == null) {
                throw new IllegalStateException("Soft rule deltas must be defined");
            }

            deltas.validate();
        }
    }


    //  NEW → DELTA CONFIG
    @Getter
    @Setter
    public static class Delta {

        private double low;
        private double medium;
        private double high;
        private double critical;

        public void validate() {

            if (low < 0 || medium < 0 || high < 0 || critical < 0) {
                throw new IllegalStateException("Delta values must be non-negative");
            }

            if (low > medium || medium > high || high > critical) {
                throw new IllegalStateException("Delta values must be ordered: low ≤ medium ≤ high ≤ critical");
            }

            if (critical > 1.0) {
                throw new IllegalStateException("Delta values must not exceed 1.0");
            }
        }
    }
}