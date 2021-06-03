package com.fedexu.binancebot.wss.macd;

public enum MACD {

    MACD_9(9),
    MACD_12(12),
    MACD_26(26);

    private final int valueId;

    MACD(int valueId) {
        this.valueId = valueId;
    }

    public int getValueId() {
        return this.valueId;
    }

    public static MACD fromInt(int number) {
        for (MACD b : MACD.values()) {
            if (b.valueId == number) {
                return b;
            }
        }
        return null;
    }

}
