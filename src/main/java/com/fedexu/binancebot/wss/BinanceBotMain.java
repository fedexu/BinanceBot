package com.fedexu.binancebot.wss;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.fedexu.binancebot.wss.analyze.CandelStickObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

@Component
public class BinanceBotMain {

    Logger logger = LoggerFactory.getLogger(BinanceBotMain.class);
    public volatile boolean exitCondition;

    @Autowired
    CandelStickObserver candelStickObserver;

    @Value("${binance.interval}")
    String TIME_INTERVAL;

    @Value("${binance.coin}")
    String COIN;

    //Dead man's solution
    @Scheduled(fixedDelay = Long.MAX_VALUE)
    public void run() {
        logger.info("BinanceWebSocketReader STARTED!");
        try {
            Closeable webSocketConnection = candelStickObserver.watchEMA(CandlestickInterval.valueOf(TIME_INTERVAL), COIN);
            while (!exitCondition) {
                // if the last socket time is below 1m
                if (System.currentTimeMillis() - candelStickObserver.webSocketEventTime > 60 * 1000) {
                    webSocketConnection.close();
                    logger.info("Close the old connection. Start a new one!");
                    webSocketConnection = candelStickObserver.watchEMA(CandlestickInterval.valueOf(TIME_INTERVAL), COIN);
                    TimeUnit.SECONDS.sleep(30);
                }
                TimeUnit.SECONDS.sleep(15);
            }
        } catch (Exception e) {
            logger.error("exception occurs in WSS Thread : ", e);
        }
    }


}





