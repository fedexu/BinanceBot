package com.fedexu.binancebot.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class EmaEvent extends ApplicationEvent {

    private double priceExcanged;
    private Double fastEma;
    private Double mediumEma;
    private Double slowEma;

    public EmaEvent(Object source, double priceExcanged, Double fastEma, Double mediumEma, Double slowEma) {
        super(source);
        this.priceExcanged = priceExcanged;
        this.fastEma = fastEma;
        this.mediumEma = mediumEma;
        this.slowEma = slowEma;
    }
}
