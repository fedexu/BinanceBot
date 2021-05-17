package com.fedexu.binancebot.wss.analyze;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.fedexu.binancebot.event.MarketStatus;
import com.fedexu.binancebot.event.OrderStatus;
import com.fedexu.binancebot.event.OrderStatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import static com.fedexu.binancebot.wss.analyze.EMA.*;
import static java.util.Objects.isNull;

@Service
public class CandelStickObserver implements ApplicationEventPublisherAware {

    public long webSocketEventTime = System.currentTimeMillis();
    Logger logger = LoggerFactory.getLogger(CandelStickObserver.class);
    private ApplicationEventPublisher publisher;

    @Autowired
    BinanceApiRestClient restClient;

    @Autowired
    BinanceApiWebSocketClient wsClient;

    @Autowired
    TaLibFunction taLibFunction;

    @Value("${binance.cache.max}")
    int MAX_CACHE_HISTORY_VALUE;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    public Closeable watchEMA(CandlestickInterval interval, String tradeSymbol) throws IOException {
        logger.info(this.toString());
        CandelStickTimesFrame timesFrame = CandelStickTimesFrame.calculateCandlestickTimesFrame(interval, MAX_CACHE_HISTORY_VALUE);

        List<Candlestick> candelsHistory = restClient.getCandlestickBars(tradeSymbol, interval, 500, timesFrame.getStart(), timesFrame.getEnd());

        return wsClient.onCandlestickEvent(tradeSymbol.toLowerCase(), interval, (candlestickEvent) -> {
            webSocketEventTime = candlestickEvent.getEventTime();
            try {
                candelDataFetch(candelsHistory, candlestickEvent);
                analyzeEMA(candelsHistory);

            } catch (Exception e) {
                logger.error("error : ", e);
            }
        });
    }

    private void analyzeEMA(List<Candlestick> candelsHistory) {
        double[] out;
        Double ema7;
        Double ema25;
        Double ema99;

        out = taLibFunction.ema(candelsHistory.stream().mapToDouble(value -> Double.parseDouble(value.getClose())).toArray(), EMA_7.getValueId());
        ema7 = (out.length - EMA_7.getValueId() >= 0) ? out[out.length - EMA_7.getValueId()] : null;
        out = taLibFunction.ema(candelsHistory.stream().mapToDouble(value -> Double.parseDouble(value.getClose())).toArray(), EMA_25.getValueId());
        ema25 = (out.length - EMA_25.getValueId() >= 0) ? out[out.length - EMA_25.getValueId()] : null;
        out = taLibFunction.ema(candelsHistory.stream().mapToDouble(value -> Double.parseDouble(value.getClose())).toArray(), EMA_99.getValueId());
        ema99 = (out.length - EMA_99.getValueId() >= 0) ? out[out.length - EMA_99.getValueId()] : null;

        logger.info("EMA(" + EMA_7 + "): " + ema7);
        logger.info("EMA(" + EMA_25 + "): " + ema25);
        logger.info("EMA(" + EMA_99 + "): " + ema99);

        if (!isNull(ema7) && !isNull(ema25) && !isNull(ema99)) {
            // pad %
            double safePad = ema25 * 0.02;

            if (ema7 < ema99 && ema25 < ema99) {
                if (ema7 < ema25 - safePad) {
                    publisher.publishEvent(new OrderStatusEvent(this, System.currentTimeMillis(), MarketStatus.LOWERING_SELL, OrderStatus.SELL));
                } else if (ema7 > ema25 + safePad) {
                    publisher.publishEvent(new OrderStatusEvent(this, System.currentTimeMillis(), MarketStatus.LOWERING_TRADING, OrderStatus.BUY));
                } else {
                    logger.info("waiting for market to adjust");
                }
            } else if (ema7 > ema99 && ema25 > ema99) {
                if (ema7 < ema25 - safePad) {
                    publisher.publishEvent(new OrderStatusEvent(this, System.currentTimeMillis(), MarketStatus.RAISING_TRADING, OrderStatus.SELL));
                } else if (ema7 > ema25 + safePad) {
                    publisher.publishEvent(new OrderStatusEvent(this, System.currentTimeMillis(), MarketStatus.RAISING_HOLD, OrderStatus.BUY));
                } else {
                    logger.info("waiting for market to adjust");
                }
            } else {
                logger.info("Inconsinstent STATE");
                logger.info("EMA(" + EMA_7 + "): " + ema7);
                logger.info("EMA(" + EMA_25 + "): " + ema25);
                logger.info("EMA(" + EMA_99 + "): " + ema99);
            }
        }

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
