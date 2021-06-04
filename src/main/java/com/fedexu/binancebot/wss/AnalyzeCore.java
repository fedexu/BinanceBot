package com.fedexu.binancebot.wss;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.fedexu.binancebot.wss.ema.EmaEvent;
import com.fedexu.binancebot.wss.macd.MacdEvent;
import com.fedexu.binancebot.wss.rsi.RsiEvent;
import com.fedexu.binancebot.wallet.order.MarketStatus;
import com.fedexu.binancebot.wallet.order.OrderStatus;
import com.fedexu.binancebot.wallet.order.OrderStatusDto;
import com.fedexu.binancebot.wallet.order.OrderStatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static java.util.Objects.isNull;

@Service
public class AnalyzeCore {

    Logger logger = LoggerFactory.getLogger(AnalyzeCore.class);

    @Autowired
    private ApplicationEventPublisher publisher;

    @Value("${binance.pad%}")
    double safePadPerc;

    @Value("${binance.interval}")
    String TIME_INTERVAL;

    public double priceExcanged;
    //EMA
    public Double fastEma;
    public Double mediumEma;
    public Double slowEma;
    //MACD
    public Double macd;
    public Double signal;
    public Double hist;
    //RSI
    public Double fastRsi;
    public Double mediumRsi;
    public Double slowRsi;

    public MarketStatus marketStatus = null;
    public OrderStatus orderStatus;

    @EventListener
    public void onEmaEvent(EmaEvent emaEvent) {
        this.priceExcanged = emaEvent.getPriceExcanged();
        this.fastEma = emaEvent.getFastEma();
        this.mediumEma = emaEvent.getMediumEma();
        this.slowEma = emaEvent.getSlowEma();
    }

    @EventListener
    public void onMacdEvent(MacdEvent macdEvent) {
        this.priceExcanged = macdEvent.getPriceExcanged();
        this.macd = macdEvent.getMacd();
        this.signal = macdEvent.getSignal();
        this.hist = macdEvent.getHist();
    }

    @EventListener
    public void onRsiEvent(RsiEvent rsiEvent) {
        this.priceExcanged = rsiEvent.getPriceExcanged();
        this.fastRsi = rsiEvent.getFastRsi();
        this.mediumRsi = rsiEvent.getMediumRsi();
        this.slowRsi = rsiEvent.getSlowRsi();
    }

    @EventListener
    public void doLogic(NewCandleStickEvent newCandleStickEvent) {
        MarketStatus actualMarketStatus = null;
        orderStatus = null;

        if (!isNull(fastEma) && !isNull(mediumEma) && !isNull(slowEma)) {
            // pad %
            double safePad = mediumEma * safePadPerc;

            if (fastEma < slowEma && mediumEma < slowEma) {
                if (fastEma < mediumEma - safePad) {
                    actualMarketStatus = MarketStatus.LOWERING_SELL;
                    orderStatus = OrderStatus.SELL;
                } else if (fastEma > mediumEma + safePad) {
                    actualMarketStatus = MarketStatus.LOWERING_TRADING;
                    orderStatus = OrderStatus.BUY;
                }
//                    waiting for market to adjust
//                    logger.info("waiting for market to adjust");
            } else if (fastEma > slowEma && mediumEma > slowEma) {
                if (fastEma < mediumEma - safePad) {
                    actualMarketStatus = MarketStatus.RAISING_TRADING;
                    orderStatus = OrderStatus.SELL;
                } else if (fastEma > mediumEma + safePad) {
                    actualMarketStatus = MarketStatus.RAISING_HOLD;
                    orderStatus = OrderStatus.BUY;
                }
//                    waiting for market to adjust
//                    logger.info("waiting for market to adjust");
            }

        }
        //NOT AN INTEREST STATES
//            else {
//                logger.info("Inconsinstent STATE");
//                logger.info("EMA(" + EMA_7 + "): " + fastEma);
//                logger.info("EMA(" + EMA_25 + "): " + mediumEma);
//                logger.info("EMA(" + EMA_99 + "): " + slowEma);
//            }
//            else if (fastEma > slowEma && fastEma > mediumEma) {
//                marketStatus = MarketStatus.RAISING_TRADING;
//                orderStatus = OrderStatus.BUY;
//            } else if (fastEma < slowEma && mediumEma > slowEma) {
//                marketStatus = MarketStatus.LOWERING_TRADING;
//                orderStatus = OrderStatus.SELL;
//            } else {
//                logger.info("Inconsinstent STATE");
//                logger.info("EMA(" + EMA_7 + "): " + fastEma);
//                logger.info("EMA(" + EMA_25 + "): " + mediumEma);
//                logger.info("EMA(" + EMA_99 + "): " + slowEma);
//            }

        if (!isNull(actualMarketStatus)) {
            if (marketStatus != actualMarketStatus) {
                marketStatus = actualMarketStatus;
                publisher.publishEvent(new OrderStatusEvent(this, buildEvent(newCandleStickEvent.getCandlestickEvent().getSymbol())));
            }
        }
    }

    private double calculatePad() throws IOException {
        long seconds = CandelStickTimesFrame.secondsInTimeFrame(CandlestickInterval.valueOf(TIME_INTERVAL));
        //TODO

        return 0.0;
    }

    private OrderStatusDto buildEvent(String symbol) {
        return OrderStatusDto.builder()
                .marketStatus(marketStatus).orderStatus(orderStatus).priceExcanged(priceExcanged)
                .fastEma(fastEma).slowEma(slowEma).mediumEma(mediumEma)
                .macd(macd).signal(signal).hist(hist)
                .fastRsi(fastRsi).mediumRsi(mediumRsi).slowRsi(slowRsi)
                .SYMBOL(symbol)
                .build();
    }
}
