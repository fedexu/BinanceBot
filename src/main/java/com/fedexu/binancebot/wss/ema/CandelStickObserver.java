package com.fedexu.binancebot.wss.ema;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.fedexu.binancebot.event.EmaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import static com.fedexu.binancebot.wss.ema.EMA.*;

@Service
public class CandelStickObserver {

    public long webSocketEventTime = System.currentTimeMillis();
    Logger logger = LoggerFactory.getLogger(CandelStickObserver.class);

    @Autowired
    BinanceApiRestClient restClient;

    @Autowired
    BinanceApiWebSocketClient wsClient;

    @Autowired
    TaLibFunction taLibFunction;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Value("${binance.cache.max}")
    int MAX_CACHE_HISTORY_VALUE;

    @Value("${binance.interval}")
    String TIME_INTERVAL;

    @Value("${binance.coin}")
    String COIN;

    public Closeable watchEMA() throws IOException {
        CandelStickTimesFrame timesFrame = CandelStickTimesFrame.calculateCandlestickTimesFrame(CandlestickInterval.valueOf(TIME_INTERVAL), MAX_CACHE_HISTORY_VALUE);

        List<Candlestick> candelsHistory = restClient.getCandlestickBars(COIN, CandlestickInterval.valueOf(TIME_INTERVAL), 500, timesFrame.getStart(), timesFrame.getEnd());

        return wsClient.onCandlestickEvent(COIN.toLowerCase(), CandlestickInterval.valueOf(TIME_INTERVAL), (candlestickEvent) -> {
            webSocketEventTime = candlestickEvent.getEventTime();
            try {
                candelDataFetch(candelsHistory, candlestickEvent);
                calculateEMA(candelsHistory);
            } catch (Exception e) {
                logger.error("error : ", e);
            }
        });
    }

    private void calculateEMA(List<Candlestick> candelsHistory) {
        double[] out;
        Double fastEma;
        Double mediumEma;
        Double slowEma;

        out = taLibFunction.ema(candelsHistory.stream().mapToDouble(value -> Double.parseDouble(value.getClose())).toArray(), EMA_7.getValueId());
        fastEma = (out.length - 1 >= 0) ? out[out.length - 1] : null;
        out = taLibFunction.ema(candelsHistory.stream().mapToDouble(value -> Double.parseDouble(value.getClose())).toArray(), EMA_25.getValueId());
        mediumEma = (out.length - 1 >= 0) ? out[out.length - 1] : null;
        out = taLibFunction.ema(candelsHistory.stream().mapToDouble(value -> Double.parseDouble(value.getClose())).toArray(), EMA_99.getValueId());
        slowEma = (out.length - 1 >= 0) ? out[out.length - 1] : null;

        publisher.publishEvent(new EmaEvent(this, Double.parseDouble(candelsHistory.get(candelsHistory.size() - 1).getClose()), fastEma, mediumEma, slowEma));
    }


    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void candelDataFetch(List<Candlestick> candelsHistory, CandlestickEvent candlestickEvent) {
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
