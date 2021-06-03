package com.fedexu.binancebot.event;

import com.binance.api.client.domain.event.CandlestickEvent;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class NewCandleStickEvent extends ApplicationEvent {

    private CandlestickEvent candlestickEvent;

    public NewCandleStickEvent(Object source, CandlestickEvent candlestickEvent) {
        super(source);
        this.candlestickEvent = candlestickEvent;
    }
}
