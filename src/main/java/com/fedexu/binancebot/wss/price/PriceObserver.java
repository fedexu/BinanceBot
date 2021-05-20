package com.fedexu.binancebot.wss.price;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.TickerPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PriceObserver {

    Logger logger = LoggerFactory.getLogger(PriceObserver.class);

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    BinanceApiRestClient restClient;

    @Scheduled(cron = "*/15 * * * * *")
    public void run() {
        List<TickerPrice> tickerPrice = restClient.getAllPrices();

    }

}