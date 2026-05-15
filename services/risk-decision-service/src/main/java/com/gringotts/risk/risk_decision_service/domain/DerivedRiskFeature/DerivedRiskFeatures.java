package com.gringotts.risk.risk_decision_service.domain.DerivedRiskFeature;

public class DerivedRiskFeatures {

    private final boolean newDevice;
    private final boolean isOddHour;
    public DerivedRiskFeatures(boolean newDevice, boolean isOddHour) {
        this.newDevice = newDevice;
        this.isOddHour = isOddHour;
    }

    public boolean isNewDevice() {
        return newDevice;
    }

    public boolean isOddHour() {
        return isOddHour;
    }
}
