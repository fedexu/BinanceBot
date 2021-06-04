package com.fedexu.binancebot.telegram.commands.Runner;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.fedexu.binancebot.configuration.runtime.BinanceBotMainRunner;
import com.fedexu.binancebot.telegram.commands.TelegramCommandEvent;
import com.fedexu.binancebot.telegram.TelegramHelper;
import com.fedexu.binancebot.telegram.commands.Start;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import static com.binance.api.client.domain.market.CandlestickInterval.*;

@Service
public class Runner {

    Logger logger = LoggerFactory.getLogger(Start.class);

    @Value("${telegram.commands.runner}")
    private String RUNNER_COMMAND;

    @Autowired
    BinanceBotMainRunner binanceBotMainRunner;

    @Autowired
    private ApplicationEventPublisher publisher;

    @EventListener
    public void onApplicationEvent(TelegramCommandEvent telegramCommandEvent) {
        long chatId = telegramCommandEvent.getTelegramCommandDto().getChatId();
        if (telegramCommandEvent.getTelegramCommandDto().getCommand().startsWith(RUNNER_COMMAND)) {
            String[] args = telegramCommandEvent.getTelegramCommandDto().getCommand()
                    .replace(RUNNER_COMMAND + " ", "").split(" ");

            CandlestickInterval interval = from(args[1]);

            binanceBotMainRunner.addBean(args[0] + "_" + interval, args[0], interval);

            publisher.publishEvent(new RunnerEvent(this, args[0], interval, chatId));
        }
    }

    private CandlestickInterval from(String interval) {
        switch (interval) {
            case "1m":
                return ONE_MINUTE;
            case "3m":
                return THREE_MINUTES;
            case "5m":
                return FIVE_MINUTES;
            case "15m":
                return FIFTEEN_MINUTES;
            case "30m":
                return HALF_HOURLY;
            case "1h":
                return HOURLY;
            case "2h":
                return TWO_HOURLY;
            case "4h":
                return FOUR_HOURLY;
            case "6h":
                return SIX_HOURLY;
            case "8h":
                return EIGHT_HOURLY;
            case "12h":
                return TWELVE_HOURLY;
            case "1d":
                return DAILY;
            case "3d":
                return THREE_DAILY;
            case "1w":
                return WEEKLY;
            case "1M":
                return MONTHLY;
            default:
                return FIFTEEN_MINUTES;
        }
    }

}