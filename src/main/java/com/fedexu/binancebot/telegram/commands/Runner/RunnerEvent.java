package com.fedexu.binancebot.telegram.commands.Runner;

import com.binance.api.client.domain.market.CandlestickInterval;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;


@Getter
@Setter
public class RunnerEvent extends ApplicationEvent {

    private String symbol;
    private CandlestickInterval Interval;
    private long chatId;

    public RunnerEvent(Object source, String symbol, CandlestickInterval Interval, long chatId) {
        super(source);
        this.symbol = symbol;
        this.Interval = Interval;
        this.chatId = chatId;
    }
}
