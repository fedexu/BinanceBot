package com.fedexu.binancebot.wss;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.account.Order;
import com.binance.api.client.domain.account.request.AllOrdersRequest;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.domain.market.TickerPrice;
import com.fedexu.binancebot.email.SendGridHelper;
import com.fedexu.binancebot.telegram.TelegramBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

public class BinanceWebSocketReader implements DisposableBean, Runnable {

    @Autowired
    SendGridHelper sendGridHelper;

    @Autowired
    TelegramBot telegramBot;

    BinanceApiRestClient restClient;
    BinanceApiWebSocketClient wsClient;

    Logger logger = LoggerFactory.getLogger(BinanceWebSocketReader.class);

    private Thread thread;
    private volatile boolean exitCondition;

    public BinanceWebSocketReader(String apiKey, String secretKey){
        BinanceApiClientFactory factory = BinanceApiClientFactory
                .newInstance(apiKey, secretKey);

        restClient = factory.newRestClient();
        wsClient = factory.newWebSocketClient();

        logger.info("BinanceWebSocketReader Configured!");

        this.thread = new Thread(this);
        this.thread.start();

    }

    @Override
    public void run(){
        List<TickerPrice> prices = restClient.getAllPrices();

//        prices.forEach(tickerPrice -> System.out.println(tickerPrice.toString()));
        AllOrdersRequest request = new AllOrdersRequest("DOGEBUSD").limit(50);
        request.recvWindow(50000L);
        List<Order> orders = restClient.getAllOrders(request);

        orders.forEach(order -> System.out.println(order.toString()));

        wsClient.onCandlestickEvent("dogebusd", CandlestickInterval.ONE_MINUTE, (candlestickEvent) ->{
            logger.info(candlestickEvent.toString());
            telegramBot.sendMessageToSubscribed(candlestickEvent.toString());
        });

        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(Objects.requireNonNull(classLoader.getResource("sendGridTemplate/sendGridEmailBody.json")).getFile());
            sendGridHelper.sendMail(new String(Files.readAllBytes(file.toPath())));
            logger.info("mail Sended");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy(){
        exitCondition = false;
    }

}






