package com.fedexu.binancebot.wss;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.fedexu.binancebot.event.EmaEvent;
import com.fedexu.binancebot.event.MarketStatus;
import com.fedexu.binancebot.event.order.OrderStatus;
import com.fedexu.binancebot.event.order.OrderStatusDto;
import com.fedexu.binancebot.event.order.OrderStatusEvent;
import com.fedexu.binancebot.wss.ema.CandelStickTimesFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.fedexu.binancebot.wss.ema.EMA.*;
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
    public Double fastEma;
    public Double mediumEma;
    public Double slowEma;
    public MarketStatus marketStatus = null;
    public OrderStatus orderStatus;

    @EventListener
    public void onEmaEvent(EmaEvent emaEvent) {
        this.priceExcanged = emaEvent.priceExcanged;
        this.fastEma = emaEvent.fastEma;
        this.mediumEma = emaEvent.mediumEma;
        this.slowEma = emaEvent.slowEma;
        doLogic();
    }

    public void doLogic() {
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

//        logger.info("Safe pad is : "+ safePad +" EMA(" + EMA_7 + "): " + fastEma + " EMA(" + EMA_25 + "): " + mediumEma + " EMA(" + EMA_99 + "): " + slowEma);

        if (!isNull(actualMarketStatus)) {
            if (marketStatus != actualMarketStatus) {
                marketStatus = actualMarketStatus;
                publisher.publishEvent(new OrderStatusEvent(this, buildEvent()));
            }
        }
    }

    private double calculatePad() throws IOException {
        long seconds = CandelStickTimesFrame.secondsInTimeFrame(CandlestickInterval.valueOf(TIME_INTERVAL));


        return 0.0;
    }

    private OrderStatusDto buildEvent() {
        return OrderStatusDto.builder()
                .marketStatus(marketStatus).orderStatus(orderStatus)
                .fastEma(fastEma).slowEma(slowEma).mediumEma(mediumEma).priceExcanged(priceExcanged)
                .build();
    }
}
