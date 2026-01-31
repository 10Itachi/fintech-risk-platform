package com.gringotts.risk.risk_decision_service.domain.decision;

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
    private BigDecimal maxSingleAmount;
    private BigDecimal maxDailyAmount;
    private Set<String> blockedCountries;
    private BigDecimal cardWithdrawalLimit;
    private Duration maxFutureDrift;
    private Duration maxPastDrift;
    // ML thresholds
    private double declineThreshold;   // e.g. 0.80
    private double reviewThreshold;    // e.g. 0.50

    // Soft rule thresholds
    private int softDeclineScore;      // e.g. 80
    private int softReviewScore;       // e.g. 40

    //policies version
    private String version;
    private Instant activatedAt;
}
