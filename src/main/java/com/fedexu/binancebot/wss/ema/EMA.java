package com.fedexu.binancebot.wss.ema;

public enum EMA {

    EMA_7(7),
    EMA_25(25),
    EMA_99(99);

    private final int valueId;

    EMA(int valueId) {
        this.valueId = valueId;
    }

    public int getValueId() {
        return this.valueId;
    }

    public static EMA fromInt(int number) {
        for (EMA b : EMA.values()) {
            if (b.valueId == number) {
                return b;
            }
        }
        return null;
    }

}
