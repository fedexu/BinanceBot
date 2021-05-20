package com.fedexu.binancebot.event.commands.notify;

public enum Direction {
    UP("UP"),
    DOWN("DOWN");

    private final String valueId;

    Direction(String valueId) {
        this.valueId = valueId;
    }

    public String getValueId() {
        return this.valueId;
    }

    public static Direction from(String value) {
        for (Direction status : Direction.values()) {
            if (status.valueId.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }
}
