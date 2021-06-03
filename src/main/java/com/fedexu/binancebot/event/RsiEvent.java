package com.fedexu.binancebot.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class RsiEvent extends ApplicationEvent {

    private double priceExcanged;
    private Double fastRsi;
    private Double mediumRsi;
    private Double slowRsi;

    public RsiEvent(Object source, double priceExcanged, Double fastRsi, Double mediumRsi, Double slowRsi) {
        super(source);
        this.priceExcanged = priceExcanged;
        this.fastRsi = fastRsi;
        this.mediumRsi = mediumRsi;
        this.slowRsi = slowRsi;
    }
}
