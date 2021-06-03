package com.fedexu.binancebot.event.order;

public enum MarketStatus {

    LOWERING_SELL("LOWERING_SELL"),
    LOWERING_TRADING("LOWERING_TRADING"),
    RAISING_TRADING("RAISING_TRADING"),
    RAISING_HOLD("RAISING_HOLD");

    private final String valueId;

    MarketStatus(String valueId) {
        this.valueId = valueId;
    }

    public String getValueId() {
        return this.valueId;
    }

    public static MarketStatus from(String value) {
        for (MarketStatus status : MarketStatus.values()) {
            if (status.valueId.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }
}
