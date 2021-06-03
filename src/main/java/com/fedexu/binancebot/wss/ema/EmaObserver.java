package com.fedexu.binancebot.wss.ema;

import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.market.Candlestick;
import com.fedexu.binancebot.event.EmaEvent;
import com.fedexu.binancebot.wss.talib.TaLibFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.fedexu.binancebot.wss.ema.EMA.*;
import static java.lang.Double.parseDouble;

@Service
public class EmaObserver {

    Logger logger = LoggerFactory.getLogger(EmaObserver.class);

    @Autowired
    TaLibFunctions taLibFunctions;

    @Autowired
    private ApplicationEventPublisher publisher;

    public void calculateEMA(List<Candlestick> candelsHistory) {
        double[] out;
        Double fastEma;
        Double mediumEma;
        Double slowEma;

        out = taLibFunctions.ema(candelsHistory.stream().mapToDouble(value -> parseDouble(value.getClose())).toArray(), EMA_7.getValueId());
        fastEma = (out.length - 1 >= 0) ? out[out.length - 1] : null;
        out = taLibFunctions.ema(candelsHistory.stream().mapToDouble(value -> parseDouble(value.getClose())).toArray(), EMA_25.getValueId());
        mediumEma = (out.length - 1 >= 0) ? out[out.length - 1] : null;
        out = taLibFunctions.ema(candelsHistory.stream().mapToDouble(value -> parseDouble(value.getClose())).toArray(), EMA_99.getValueId());
        slowEma = (out.length - 1 >= 0) ? out[out.length - 1] : null;

        publisher.publishEvent(new EmaEvent(this, parseDouble(candelsHistory.get(candelsHistory.size() - 1).getClose()), fastEma, mediumEma, slowEma));
    }

}
