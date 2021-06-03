package com.fedexu.binancebot.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class MacdEvent extends ApplicationEvent {

    private double priceExcanged;
    private Double macd;
    private Double signal;
    private Double hist;

    public MacdEvent(Object source, double priceExcanged, Double macd, Double signal, Double hist) {
        super(source);
        this.priceExcanged = priceExcanged;
        this.macd = macd;
        this.signal = signal;
        this.hist = hist;
    }
}
