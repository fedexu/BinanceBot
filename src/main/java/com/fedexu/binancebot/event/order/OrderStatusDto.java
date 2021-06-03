package com.fedexu.binancebot.event.order;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderStatusDto {

    private OrderStatus orderStatus;
    private MarketStatus marketStatus;
    private double priceExcanged;
    private Double fastEma;
    private Double mediumEma;
    private Double slowEma;

}
