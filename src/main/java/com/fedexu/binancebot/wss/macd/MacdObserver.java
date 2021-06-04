package com.fedexu.binancebot.wss.macd;

import com.binance.api.client.domain.market.Candlestick;
import com.fedexu.binancebot.wss.talib.MacdValues;
import com.fedexu.binancebot.wss.talib.TaLibFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.fedexu.binancebot.wss.macd.MACD.*;
import static java.lang.Double.parseDouble;

@Service
public class MacdObserver {
    Logger logger = LoggerFactory.getLogger(MacdObserver.class);

    @Autowired
    TaLibFunctions taLibFunctions;

    @Autowired
    private ApplicationEventPublisher publisher;

    public void calculateMACD(List<Candlestick> candelsHistory) {
        double[] out;

        MacdValues values =
                taLibFunctions.macd(candelsHistory.stream().mapToDouble(value -> parseDouble(value.getClose())).toArray(),
                        MACD_12.getValueId(), MACD_26.getValueId(), MACD_9.getValueId());

        publisher.publishEvent(new MacdEvent(this,
                parseDouble(candelsHistory.get(candelsHistory.size() - 1).getClose()),
                values.getMacd(), values.getSignal(), values.getHist()));
    }

}
