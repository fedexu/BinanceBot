package com.fedexu.binancebot.wss;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.fedexu.binancebot.email.SendGridHelper;
import com.fedexu.binancebot.telegram.TelegramBot;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import static com.fedexu.binancebot.wss.EMA.*;

@Service
public class CandelStickObserver {

    public long webSocketEventTime = System.currentTimeMillis();
    Logger logger = LoggerFactory.getLogger(CandelStickObserver.class);

    @Autowired
    SendGridHelper sendGridHelper;

    @Autowired
    TelegramBot telegramBot;

    @Autowired
    BinanceApiRestClient restClient;

    @Autowired
    BinanceApiWebSocketClient wsClient;

    @Autowired
    TaLibFunction taLibFunction;

    @Value("${binance.cache.max}")
    int MAX_CACHE_HISTORY_VALUE;

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public Closeable watchEMA(CandlestickInterval interval, String tradeSymbol) throws IOException {
        logger.info(this.toString());
        CandelStickTimesFrame timesFrame = CandelStickTimesFrame.calculateCandlestickTimesFrame(interval, MAX_CACHE_HISTORY_VALUE);

        List<Candlestick> candelsHistory = restClient.getCandlestickBars(tradeSymbol, interval, 500, timesFrame.getStart(), timesFrame.getEnd());

        return wsClient.onCandlestickEvent(tradeSymbol.toLowerCase(), interval, (candlestickEvent) -> {
            webSocketEventTime = candlestickEvent.getEventTime();
            try {
                //search if new Candel Time is occur
                if (candelsHistory.stream().map(Candlestick::getOpenTime)
                        .noneMatch(candlestickEvent.getOpenTime()::equals)) {
                    //remove the oldest value and update the last element with the new Candel event created
                    if (candelsHistory.size() >= MAX_CACHE_HISTORY_VALUE) {
                        logger.info("REMOVING CANDEL : " + candelsHistory.get(0).toString());
                        candelsHistory.remove(0);
                    }
                    candelsHistory.add(toCandlestick(candlestickEvent));
                    logger.info("ADDING : " + candelsHistory.get(candelsHistory.size() - 1).toString());
                } else {
                    //update the current candel close value
                    candelsHistory.stream()
                            .filter(candelValue -> candelValue.getOpenTime().equals(candlestickEvent.getOpenTime()))
                            .findFirst().get().setClose(candlestickEvent.getClose());
                }
                logger.info("CLOSED PRICE : " + Double.parseDouble(candlestickEvent.getClose()));

                double[] out;

                out = taLibFunction.ema(candelsHistory.stream().mapToDouble(value -> Double.parseDouble(value.getClose())).toArray(), EMA_7.getValueId());
                logger.info("EMA(" + EMA_7 + "): " + ((out.length - EMA_7.getValueId() >= 0) ? out[out.length - EMA_7.getValueId()] : "NaN"));

                out = taLibFunction.ema(candelsHistory.stream().mapToDouble(value -> Double.parseDouble(value.getClose())).toArray(), EMA_25.getValueId());
                logger.info("EMA(" + EMA_25 + "): " + ((out.length - EMA_25.getValueId() >= 0) ? out[out.length - EMA_25.getValueId()] : "NaN"));

                out = taLibFunction.ema(candelsHistory.stream().mapToDouble(value -> Double.parseDouble(value.getClose())).toArray(), EMA_99.getValueId());
                logger.info("EMA(" + EMA_99 + "): " + ((out.length - EMA_99.getValueId() >= 0) ? out[out.length - EMA_99.getValueId()] : "NaN"));


            } catch (Exception e) {
                logger.error("error : ", e);
            }
        });
    }

    private Candlestick toCandlestick(CandlestickEvent event) {
        Candlestick candlestick = new Candlestick();
        candlestick.setOpenTime(event.getOpenTime());
        candlestick.setOpen(event.getOpen());
        candlestick.setHigh(event.getHigh());
        candlestick.setLow(event.getLow());
        candlestick.setClose(event.getClose());
        candlestick.setVolume(event.getVolume());
        candlestick.setCloseTime(event.getCloseTime());
        candlestick.setQuoteAssetVolume(event.getQuoteAssetVolume());
        candlestick.setNumberOfTrades(event.getNumberOfTrades());
        candlestick.setTakerBuyBaseAssetVolume(event.getTakerBuyBaseAssetVolume());
        candlestick.setTakerBuyQuoteAssetVolume(event.getTakerBuyQuoteAssetVolume());
        return candlestick;
    }
}
