package com.fedexu.binancebot.telegram.commands.notify;

public enum NotifyType {
    ADD("ADD"),
    REMOVE("REMOVE");

    private final String valueId;

    NotifyType(String valueId) {
        this.valueId = valueId;
    }

    public String getValueId() {
        return this.valueId;
    }

    public static NotifyType from(String value) {
        for (NotifyType status : NotifyType.values()) {
            if (status.valueId.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }
}
