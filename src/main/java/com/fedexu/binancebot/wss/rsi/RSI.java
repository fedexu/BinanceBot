package com.fedexu.binancebot.wss.rsi;

public enum RSI {

    RSI_6(6),
    RSI_12(12),
    RSI_24(24);

    private final int valueId;

    RSI(int valueId) {
        this.valueId = valueId;
    }

    public int getValueId() {
        return this.valueId;
    }

    public static RSI fromInt(int number) {
        for (RSI b : RSI.values()) {
            if (b.valueId == number) {
                return b;
            }
        }
        return null;
    }

}
