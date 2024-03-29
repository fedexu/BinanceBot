package com.fedexu.binancebot.wss;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.fedexu.binancebot.configuration.runtime.BinanceBotMainRunner;
import com.fedexu.binancebot.wallet.WalletManager;
import com.fedexu.binancebot.wss.ema.EmaObserver;
import com.fedexu.binancebot.wss.macd.MacdObserver;
import com.fedexu.binancebot.wss.rsi.RsiObserver;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BinanceBotMain {

    Logger logger = LoggerFactory.getLogger(BinanceBotMain.class);

    public long webSocketEventTime = System.currentTimeMillis();
    public volatile boolean exitCondition;

    @Autowired
    EmaObserver emaObserver;

    @Autowired
    MacdObserver macdObserver;

    @Autowired
    RsiObserver rsiObserver;

    @Autowired
    BinanceApiRestClient restClient;

    @Autowired
    BinanceApiWebSocketClient wsClient;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Value("${binance.cache.max}")
    int MAX_CACHE_HISTORY_VALUE;

    public CandlestickInterval TIME_INTERVAL;
    public String market;

    @Autowired
    WalletManager walletManager;

    @Autowired
    BinanceBotMainRunner binanceBotMainRunner;

    //Dead man's solution
    @SneakyThrows
    @Scheduled(fixedDelay = Long.MAX_VALUE)
    public void run() {
        if (!walletManager.addWallet(market)) {
            destory();
            return;
        }

        logger.info("BinanceWebSocketReader STARTED!");
        try {
            Closeable webSocketConnection = watcher();
            while (!exitCondition) {
                // if the last socket time is below 1m
                if (System.currentTimeMillis() - webSocketEventTime > 60 * 1000) {
                    webSocketConnection.close();
                    logger.info("Close the old connection. Start a new one!");
                    webSocketConnection = watcher();
                    TimeUnit.SECONDS.sleep(30);
                }
                TimeUnit.SECONDS.sleep(15);
            }
        } catch (IOException e) {
            logger.error("exception occurs in WSS Thread : ", e);
            destory();
        } catch (InterruptedException e) {
            logger.error("exception occurs in WSS Thread : ", e);
            destory();
            throw e;
        }
    }

    public Closeable watcher() throws IOException {
        CandelStickTimesFrame timesFrame = CandelStickTimesFrame.calculateCandlestickTimesFrame(TIME_INTERVAL, MAX_CACHE_HISTORY_VALUE);

        List<Candlestick> candelsHistory = restClient.getCandlestickBars(market, TIME_INTERVAL, 500, timesFrame.getStart(), timesFrame.getEnd());

        return wsClient.onCandlestickEvent(market.toLowerCase(), TIME_INTERVAL, (candlestickEvent) -> {
            webSocketEventTime = candlestickEvent.getEventTime();
            candelDataFetch(candelsHistory, candlestickEvent);
            emaObserver.calculateEMA(candelsHistory);
            macdObserver.calculateMACD(candelsHistory);
            rsiObserver.calculateRSI(candelsHistory);
        });
    }

    private void candelDataFetch(List<Candlestick> candelsHistory, CandlestickEvent candlestickEvent) {
        //search if new Candel Time is occur
        if (candelsHistory.stream().map(Candlestick::getOpenTime)
                .noneMatch(candlestickEvent.getOpenTime()::equals)) {
            //remove the oldest value and update the last element with the new Candel event created
            if (candelsHistory.size() >= MAX_CACHE_HISTORY_VALUE) {
//                logger.info("REMOVING CANDEL : " + candelsHistory.get(0).toString());
                candelsHistory.remove(0);
            }
            candelsHistory.add(toCandlestick(candlestickEvent));
            publisher.publishEvent(new NewCandleStickEvent(this, candlestickEvent));
//            logger.info("ADDING : " + candelsHistory.get(candelsHistory.size() - 1).toString());
        } else {
            //update the current candel close value
            candelsHistory.stream()
                    .filter(candelValue -> candelValue.getOpenTime().equals(candlestickEvent.getOpenTime()))
                    .findFirst()
                    .ifPresent(candlestick -> candlestick.setClose(candlestickEvent.getClose()));
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

    private void destory() {
        binanceBotMainRunner.removeBean(market + "_" + TIME_INTERVAL);
    }

}






