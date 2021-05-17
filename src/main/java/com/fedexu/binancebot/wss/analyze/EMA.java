package com.fedexu.binancebot.wss.analyze;

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
}
