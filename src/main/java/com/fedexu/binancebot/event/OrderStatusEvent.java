package com.fedexu.binancebot.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class OrderStatusEvent extends ApplicationEvent {

    private long eventTime;
    private OrderStatus orderStatus;
    private MarketStatus marketStatus;

    public OrderStatusEvent(Object source, long eventTime, MarketStatus marketStatus, OrderStatus orderStatus) {
        super(source);
        this.eventTime = eventTime;
        this.marketStatus = marketStatus;
        this.orderStatus = orderStatus;
    }

}
