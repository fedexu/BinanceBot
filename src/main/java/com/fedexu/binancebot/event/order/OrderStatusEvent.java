package com.fedexu.binancebot.event.order;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class OrderStatusEvent extends ApplicationEvent {

    private long eventTime;
    private OrderStatusDto orderStatusDto;

    public OrderStatusEvent(Object source, OrderStatusDto orderStatusDto) {
        super(source);
        this.eventTime = System.currentTimeMillis();
        this.orderStatusDto = orderStatusDto;
    }

}
