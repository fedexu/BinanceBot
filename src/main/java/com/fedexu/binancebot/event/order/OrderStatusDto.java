package com.fedexu.binancebot.event.order;

import com.fedexu.binancebot.event.MarketStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderStatusDto {

    private OrderStatus orderStatus;
    private MarketStatus marketStatus;
    private double priceExcanged;
}
