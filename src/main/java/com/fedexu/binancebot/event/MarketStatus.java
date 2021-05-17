package com.fedexu.binancebot.event;

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
}
