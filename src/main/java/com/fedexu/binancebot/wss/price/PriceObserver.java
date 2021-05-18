package com.fedexu.binancebot.wss.price;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.TickerPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PriceObserver implements ApplicationEventPublisherAware {

    Logger logger = LoggerFactory.getLogger(com.fedexu.binancebot.wss.analyze.CandelStickObserver.class);
    private ApplicationEventPublisher publisher;

    @Autowired
    BinanceApiRestClient restClient;

    @SuppressWarnings("NullableProblems")
    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    @Scheduled(cron = "*/15 * * * * *")
    public void run(){
        List<TickerPrice> tickerPrice = restClient.getAllPrices();

    }

}