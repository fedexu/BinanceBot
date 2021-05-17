package com.fedexu.binancebot.wss.wallet;

import com.fedexu.binancebot.email.SendGridHelper;
import com.fedexu.binancebot.event.MarketStatus;
import com.fedexu.binancebot.event.order.OrderStatusEvent;
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

    //Default in HOLD status to not do anything
    private MarketStatus marketStatus = MarketStatus.RAISING_HOLD;

    @Override
    public void onApplicationEvent(OrderStatusEvent orderStatusEvent) {
        if (orderStatusEvent.getOrderStatusDto().getMarketStatus() != marketStatus){
            marketStatus = orderStatusEvent.getOrderStatusDto().getMarketStatus();
            String excangedPrice =  " \\n Actual Excanged price is " + orderStatusEvent.getOrderStatusDto().getPriceExcanged() + " $ " ;
            switch (marketStatus){
                case LOWERING_SELL:
                    telegramBot.sendMessageToSubscribed("Decreasing market, DO NOT BUY AT ALL" + excangedPrice);
                    break;
                case LOWERING_TRADING:
                    telegramBot.sendMessageToSubscribed("Local time frame market is raising, it's not safe to HOLD " + excangedPrice);
                    break;
                case RAISING_TRADING:
                    telegramBot.sendMessageToSubscribed("Local time frame market is Decreasing, suggest to Sell " + excangedPrice);
                    break;
                case RAISING_HOLD:
                    telegramBot.sendMessageToSubscribed("Local time frame market is raising, you can buy and HOLD " + excangedPrice);
                    break;
            }

        }
    }

}
