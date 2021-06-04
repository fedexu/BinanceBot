package com.fedexu.binancebot.wss.rsi;

import com.binance.api.client.domain.market.Candlestick;
import com.fedexu.binancebot.wss.talib.TaLibFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.fedexu.binancebot.wss.rsi.RSI.*;
import static java.lang.Double.parseDouble;

@Service
public class RsiObserver {

    Logger logger = LoggerFactory.getLogger(com.fedexu.binancebot.wss.macd.MacdObserver.class);

    @Autowired
    TaLibFunctions taLibFunctions;

    @Autowired
    private ApplicationEventPublisher publisher;

    public void calculateRSI(List<Candlestick> candelsHistory) {
        double[] out;
        Double fastRsi;
        Double mediumRsi;
        Double slowRsi;

        out = taLibFunctions.rsi(candelsHistory.stream().mapToDouble(value -> parseDouble(value.getClose())).toArray(), RSI_6.getValueId());
        fastRsi = (out.length - 1 >= 0) ? out[out.length - 1] : null;
        out = taLibFunctions.rsi(candelsHistory.stream().mapToDouble(value -> parseDouble(value.getClose())).toArray(), RSI_12.getValueId());
        mediumRsi = (out.length - 1 >= 0) ? out[out.length - 1] : null;
        out = taLibFunctions.rsi(candelsHistory.stream().mapToDouble(value -> parseDouble(value.getClose())).toArray(), RSI_24.getValueId());
        slowRsi = (out.length - 1 >= 0) ? out[out.length - 1] : null;

        publisher.publishEvent(new RsiEvent(this, parseDouble(candelsHistory.get(candelsHistory.size() - 1).getClose()), fastRsi, mediumRsi, slowRsi));
    }
}
