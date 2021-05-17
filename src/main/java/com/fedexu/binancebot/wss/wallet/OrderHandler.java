package com.fedexu.binancebot.wss.wallet;

import com.fedexu.binancebot.email.SendGridHelper;
import com.fedexu.binancebot.event.MarketStatus;
import com.fedexu.binancebot.event.OrderStatus;
import com.fedexu.binancebot.event.OrderStatusEvent;
import com.fedexu.binancebot.telegram.TelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class OrderHandler implements ApplicationListener<OrderStatusEvent> {

    @Autowired
    SendGridHelper sendGridHelper;

    @Autowired
    TelegramBot telegramBot;

    private MarketStatus marketStatus;

    @Override
    public void onApplicationEvent(OrderStatusEvent orderStatusEvent) {
        if (orderStatusEvent.getMarketStatus() != marketStatus){
            marketStatus = orderStatusEvent.getMarketStatus();
            switch (marketStatus){
                case LOWERING_SELL:
                    telegramBot.sendMessageToSubscribed("Decreasing market, DO NOT BUY AT ALL");
                    break;
                case LOWERING_TRADING:
                    telegramBot.sendMessageToSubscribed("Local time frame market is raising, it's not safe to HOLD");
                    break;
                case RAISING_TRADING:
                    telegramBot.sendMessageToSubscribed("Local time frame market is Decreasing, suggest to Sell");
                    break;
                case RAISING_HOLD:
                    telegramBot.sendMessageToSubscribed("Local time frame market is raising, you can buy and HOLD");
                    break;
            }

        }
    }

}
