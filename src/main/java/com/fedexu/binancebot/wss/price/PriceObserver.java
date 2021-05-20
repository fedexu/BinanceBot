package com.fedexu.binancebot.wss.price;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.TickerPrice;
import com.fedexu.binancebot.event.commands.notify.NotifyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.fedexu.binancebot.event.commands.notify.Direction.UP;
import static com.fedexu.binancebot.event.commands.notify.NotifyType.ADD;
import static com.fedexu.binancebot.event.commands.notify.NotifyType.REMOVE;
import static java.lang.Double.parseDouble;
import static java.lang.Long.parseLong;

@Service
public class PriceObserver {

    Logger logger = LoggerFactory.getLogger(PriceObserver.class);

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    BinanceApiRestClient restClient;

    public List<NotifyEvent> pricesToCheck = new ArrayList<>();

    @Scheduled(cron = "*/15 * * * * *")
    public void run() {
        List<TickerPrice> tickerPrices = restClient.getAllPrices();
        List<NotifyEvent> toRemove = new ArrayList<>();
        tickerPrices.forEach(tickerPrice -> pricesToCheck.stream().filter(notifyEvent -> tickerPrice.getSymbol().equals(notifyEvent.getSymbol()))
                .collect(Collectors.toList()).forEach(notifyEvent -> {
            notifyEvent.setActualPrice(parseDouble(tickerPrice.getPrice()));
            if (notifyEvent.getDirection() == UP) {
                if (parseDouble(tickerPrice.getPrice()) > notifyEvent.getPrice()) {
                    toRemove.add(notifyEvent);
                    publisher.publishEvent(notifyEvent);
                }
            } else {
                if (parseDouble(tickerPrice.getPrice()) < notifyEvent.getPrice()) {
                    toRemove.add(notifyEvent);
                    publisher.publishEvent(notifyEvent);
                }
            }
        }));
        pricesToCheck.removeAll(toRemove);
    }

    @EventListener
    public void onNotifyEventEvent(NotifyEvent notifyEvent) {
        if (notifyEvent.getType() == ADD) {
            notifyEvent.setType(REMOVE);
            pricesToCheck.add(notifyEvent);
        }
    }

}