package com.gringotts.risk.risk_decision_service.domain.DerivedRiskFeature;

import com.gringotts.risk.risk_decision_service.domain.decision.RiskDecisionContext;
import com.gringotts.risk.risk_decision_service.domain.deviceid.DeviceHistoryService;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;

@Component
public class DerivedRiskFeatureBuilder {

    /*
     Service responsible for:
     - checking whether device was seen before
     - storing new device history
     - updating lastSeen timestamp
     */
    private final DeviceHistoryService deviceHistoryService;

    public DerivedRiskFeatureBuilder(
            DeviceHistoryService deviceHistoryService
    ) {
        this.deviceHistoryService = deviceHistoryService;
    }

    public DerivedRiskFeatures build(RiskDecisionContext ctx) {

        /*
         Extract request values from shared evaluation context.
         */
        String userId = ctx.getRequest().getUserId();

        String deviceId = ctx.getRequest().getDeviceId();

        Instant transactionTime =
                ctx.getRequest().getTransactionTime();

        /*
         DEVICE RISK FEATURE
         -------------------
         If deviceId missing:
         treat as suspicious/new device.

         Otherwise:
         check historical device usage from DB.
         */
        boolean isNewDevice;

        if (deviceId == null || "UNKNOWN".equals(deviceId)) {

            isNewDevice = true;

        } else {

            /*
             DeviceHistoryService internally:
             1. checks DB for existing device
             2. inserts first-time device
             3. updates lastSeenAt
             4. handles concurrent insert race safely
             */
            isNewDevice =
                    deviceHistoryService.isNewDeviceAndRecord(
                            userId,
                            deviceId,
                            transactionTime
                    );
        }

        /*
         ODD-HOUR FEATURE
         ----------------
         Convert transaction timestamp into IST timezone.

         Transactions between:
         12 AM → 4 AM

         treated as higher-risk behavior.
         */
        int hour = transactionTime
                .atZone(ZoneId.of("Asia/Kolkata"))
                .getHour();

        boolean isOddHour =
                (hour >= 0 && hour <= 4);

        /*
         Build immutable derived feature object.

         This object later gets attached into:
         RiskDecisionContext

         and reused by:
         - ML scoring
         - soft rules
         - decision policies
         */
        return new DerivedRiskFeatures(
                isNewDevice,
                isOddHour
        );
    }
}