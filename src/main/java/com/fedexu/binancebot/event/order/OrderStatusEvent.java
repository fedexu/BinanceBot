package com.fedexu.binancebot.event.order;

import com.fedexu.binancebot.event.MarketStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class OrderStatusEvent extends ApplicationEvent {

    private long eventTime;
    private OrderStatusDto orderStatusDto;

    public OrderStatusEvent(Object source, long eventTime, OrderStatusDto orderStatusDto) {
        super(source);
        this.eventTime = eventTime;
        this.orderStatusDto = orderStatusDto;
    }

}
