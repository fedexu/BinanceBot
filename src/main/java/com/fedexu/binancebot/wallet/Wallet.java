package com.fedexu.binancebot.wallet;

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
    private Double fiat = 1000.0;
    private Double coin = 0.0;
    private final Double fee = 1.001;
    private Double totalFee = 0.0;

    @Value("${binance.coin}")
    String COIN;

    @Value("${telegram.messages.order}")
    private String ORDER_MESSAGE;

    @Autowired
    SendGridHelper sendGridHelper;

    @Autowired
    private TelegramHelper telegramHelper;

    @EventListener
    public void onApplicationEvent(OrderStatusEvent orderStatusEvent) {
        OrderStatusDto orderStatusDto = orderStatusEvent.getOrderStatusDto();
        double tradeFee = 0.0;
        if (SELL == orderStatusDto.getOrderStatus() && fiat == 0) {
            fiat = coin * orderStatusDto.getPriceExcanged();
            tradeFee = (fiat / fee) - fiat;
            fiat = fiat / fee;
            coin = 0.0;
            totalFee = tradeFee;
        } else if (BUY == orderStatusDto.getOrderStatus() && coin == 0) {
            coin = fiat / orderStatusDto.getPriceExcanged();
            tradeFee = (fiat / fee) - fiat;
            fiat = fiat / fee;
            fiat = 0.0;
        }

        totalFee += tradeFee;

        String excangedPrice = "Actual Exchanged price is " + orderStatusDto.getPriceExcanged() + " $ ";
        String emaValue = "EMA(" + EMA_7.getValueId() + "): " + orderStatusDto.getFastEma() + "\n" +
                " EMA(" + EMA_25.getValueId() + "): " + orderStatusDto.getMediumEma() + "\n" +
                " EMA(" + EMA_99.getValueId() + "): " + orderStatusDto.getSlowEma();
        String typeOfOrder = "Type order is : " + orderStatusDto.getOrderStatus().getValueId();
        String wallet = "Actual wallet : FIAT " + fiat + "$ | " + COIN.replace("BUSD", "") + " " + coin;
        String fee = " Fee applied : " + String.format("%.12f", tradeFee);
        String totalFee = "Total fee payed : " + String.format("%.12f", this.totalFee);
        String message = excangedPrice + "\n" + emaValue + "\n" + typeOfOrder + "\n" + wallet + "\n" + fee + "\n" + totalFee;

        logger.info("SENDING TELEGRAM MESSAGE : " + message);
        telegramHelper.sendMessageToSubscribed(message);
    }

    private String createMessage(OrderStatusEvent orderStatusEvent, double tradeFee) {
        final String PRICE = "<PRICE>";
        final String FAST_EMA = "<FAST_EMA>";
        final String MEDIUM_EMA = "<MEDIUM_EMA>";
        final String SLOW_EMA = "<SLOW_EMA>";
        final String MACD = "<MACD>";
        final String FAST_RSI = "<FAST_RSI>";
        final String MEDIUM_RSI = "<MEDIUM_RSI>";
        final String SLOW_RSI = "<SLOW_RSI>";
        final String ORDER_TYPE = "<ORDER_TYPE>";
        final String FIAT = "<FIAT>";
        final String COIN = "<COIN>";
        final String FEE = "<FEE>";
        final String TOTAL_FEE = "<TOTAL_FEE>";
        final OrderStatusDto orderStatusDto = orderStatusEvent.getOrderStatusDto();
        return ORDER_MESSAGE
                .replace(PRICE, String.valueOf(orderStatusDto.getPriceExcanged()))
                .replace(FAST_EMA, String.valueOf(orderStatusDto.getFastEma()))
                .replace(MEDIUM_EMA, String.valueOf(orderStatusDto.getMediumEma()))
                .replace(SLOW_EMA, String.valueOf(orderStatusDto.getSlowEma()))
                .replace(MACD, String.valueOf(orderStatusDto.getMacd()))
                .replace(FAST_RSI, String.valueOf(orderStatusDto.getFastRsi()))
                .replace(MEDIUM_RSI, String.valueOf(orderStatusDto.getMediumRsi()))
                .replace(SLOW_RSI, String.valueOf(orderStatusDto.getSlowRsi()))
                .replace(ORDER_TYPE, orderStatusDto.getOrderStatus().getValueId())
                .replace(FIAT, String.valueOf(fiat))
                .replace(COIN, String.valueOf(coin))
                .replace(FEE, String.valueOf(tradeFee))
                .replace(TOTAL_FEE, String.valueOf(totalFee));
    }

}
