package com.fedexu.binancebot.event;

public enum OrderStatus {

    SELL("SELL"),
    BUY("BUY");

    private final String valueId;

    OrderStatus(String valueId) {
        this.valueId = valueId;
    }

    public String getValueId() {
        return this.valueId;
    }
}
