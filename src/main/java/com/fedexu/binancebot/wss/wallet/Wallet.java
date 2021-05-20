package com.fedexu.binancebot.wss.wallet;

import com.fedexu.binancebot.email.SendGridHelper;
import com.fedexu.binancebot.event.order.OrderStatusDto;
import com.fedexu.binancebot.event.order.OrderStatusEvent;
import com.fedexu.binancebot.telegram.TelegramHelper;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import static com.fedexu.binancebot.event.order.OrderStatus.BUY;
import static com.fedexu.binancebot.event.order.OrderStatus.SELL;
import static com.fedexu.binancebot.wss.ema.EMA.*;

@Service
@Getter
public class Wallet {

    Logger logger = LoggerFactory.getLogger(Wallet.class);

    //for test purpose
    private Double fiat = 100.0;
    private Double coin = 0.0;

    @Value("${binance.coin}")
    String COIN;

    @Autowired
    SendGridHelper sendGridHelper;

    @Autowired
    private TelegramHelper telegramHelper;

    @EventListener
    public void onApplicationEvent(OrderStatusEvent orderStatusEvent) {
        OrderStatusDto orderStatusDto = orderStatusEvent.getOrderStatusDto();
        if (SELL == orderStatusDto.getOrderStatus() && fiat == 0) {
            fiat = coin * orderStatusDto.getPriceExcanged();
            coin = 0.0;
        } else if (BUY == orderStatusDto.getOrderStatus() && coin == 0) {
            coin = fiat / orderStatusDto.getPriceExcanged();
            fiat = 0.0;
        }

        String excangedPrice = "Actual Exchanged price is " + orderStatusDto.getPriceExcanged() + " $ ";
        String emaValue = "EMA(" + EMA_7.getValueId() + "): " + orderStatusDto.getFastEma() + "\n" +
                " EMA(" + EMA_25.getValueId() + "): " + orderStatusDto.getMediumEma() + "\n" +
                " EMA(" + EMA_99.getValueId() + "): " + orderStatusDto.getSlowEma();
        String wallet = "Actual wallet : FIAT " + fiat + "$ | " + COIN.replace("BUSD", "") + " " + coin;
        String typeOfOrder = "Type order is : " + orderStatusDto.getOrderStatus().getValueId();
        String message = excangedPrice + "\n" + emaValue + "\n" + typeOfOrder + "\n" +  wallet;

        logger.info("SENDING TELEGRAM MESSAGE : " + message);
        telegramHelper.sendMessageToSubscribed(message);
    }

}
