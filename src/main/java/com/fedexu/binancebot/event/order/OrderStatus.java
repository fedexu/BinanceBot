package com.fedexu.binancebot.event.order;

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

    public static OrderStatus fromString(String value) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.valueId.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }
}
