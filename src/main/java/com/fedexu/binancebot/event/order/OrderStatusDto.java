package com.fedexu.binancebot.event.order;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderStatusDto {

    private OrderStatus orderStatus;
    private MarketStatus marketStatus;
    private double priceExcanged;
    //EMA
    private Double fastEma;
    private Double mediumEma;
    private Double slowEma;
    //MACD
    private Double macd;
    private Double signal;
    private Double hist;
    //RSI
    private Double fastRsi;
    private Double mediumRsi;
    private Double slowRsi;

}
