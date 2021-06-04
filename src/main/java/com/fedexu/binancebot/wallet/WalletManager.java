package com.fedexu.binancebot.wallet;

import com.fedexu.binancebot.email.SendGridHelper;
import com.fedexu.binancebot.event.order.OrderStatusDto;
import com.fedexu.binancebot.event.order.OrderStatusEvent;
import com.fedexu.binancebot.telegram.TelegramHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.fedexu.binancebot.event.order.OrderStatus.BUY;
import static com.fedexu.binancebot.event.order.OrderStatus.SELL;
import static java.util.Objects.isNull;

@Service
public class WalletManager {

    Logger logger = LoggerFactory.getLogger(WalletManager.class);

    Map<String, Wallet> wallet = new HashMap<>();

    @Value("${telegram.messages.order}")
    private String ORDER_MESSAGE;

    @Autowired
    SendGridHelper sendGridHelper;

    @Autowired
    private TelegramHelper telegramHelper;

    @EventListener
    public void onApplicationEvent(OrderStatusEvent orderStatusEvent) {
        OrderStatusDto orderStatusDto = orderStatusEvent.getOrderStatusDto();
        Wallet wallet = this.wallet.get(orderStatusDto.getSYMBOL());

        if (isNull(wallet)){
            logger.error("Wallet not found!");
            return;
        }

        double tradeFee = 0.0;
        if (SELL == orderStatusDto.getOrderStatus() && wallet.getFiat() == 0) {
            wallet.setFiat(wallet.getCoin() * orderStatusDto.getPriceExcanged());
            tradeFee = Math.abs((wallet.getFiat() / wallet.getFee()) - wallet.getFiat());
            wallet.setFiat(wallet.getFiat() / wallet.getFee());
            wallet.setCoin(0.0);
        } else if (BUY == orderStatusDto.getOrderStatus() && wallet.getCoin() == 0) {
            tradeFee = Math.abs((wallet.getFiat() / wallet.getFee()) - wallet.getFiat());
            wallet.setFiat(wallet.getFiat() / wallet.getFee());
            wallet.setCoin(wallet.getFiat() / orderStatusDto.getPriceExcanged());
            wallet.setFiat(0.0);
        }

        wallet.setTotalFee(wallet.getTotalFee() + tradeFee );

        telegramHelper.sendMessageToSubscribed(createMessage(orderStatusEvent, tradeFee, wallet));
    }

    private String createMessage(OrderStatusEvent orderStatusEvent, double tradeFee, Wallet wallet) {
        final String SYMBOL = "<SYMBOL>";
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
                .replace(SYMBOL, String.valueOf(wallet.getSYMBOL()))
                .replace(PRICE, String.valueOf(orderStatusDto.getPriceExcanged()))
                .replace(FAST_EMA, String.valueOf(orderStatusDto.getFastEma()))
                .replace(MEDIUM_EMA, String.valueOf(orderStatusDto.getMediumEma()))
                .replace(SLOW_EMA, String.valueOf(orderStatusDto.getSlowEma()))
                .replace(MACD, String.valueOf(orderStatusDto.getMacd()))
                .replace(FAST_RSI, String.valueOf(orderStatusDto.getFastRsi()))
                .replace(MEDIUM_RSI, String.valueOf(orderStatusDto.getMediumRsi()))
                .replace(SLOW_RSI, String.valueOf(orderStatusDto.getSlowRsi()))
                .replace(ORDER_TYPE, orderStatusDto.getOrderStatus().getValueId())
                .replace(FIAT, String.valueOf(wallet.getFiat()))
                .replace(COIN, String.valueOf(wallet.getCoin()))
                .replace(FEE, String.valueOf(tradeFee))
                .replace(TOTAL_FEE, String.valueOf(wallet.getTotalFee()));
    }

    public boolean addWallet(String symbol){
        if (isNull(this.wallet.get(symbol))){
            wallet.put(symbol, new Wallet(symbol));
            return true;
        }else {
            logger.error("Wallet already present");
            return false;
        }
    }

    public boolean removeWallet(String symbol){
        if (isNull(this.wallet.get(symbol))){
            logger.error("Wallet not found!");
            return false;
        }else {
            this.wallet.remove(symbol);
            return true;
        }
    }

}
