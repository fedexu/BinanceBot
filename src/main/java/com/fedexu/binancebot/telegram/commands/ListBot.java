package com.fedexu.binancebot.telegram.commands;

import com.fedexu.binancebot.configuration.runtime.BinanceBotMainRunner;
import com.fedexu.binancebot.telegram.TelegramHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class ListBot {

    Logger logger = LoggerFactory.getLogger(Start.class);

    @Autowired
    private TelegramHelper telegramHelper;

    @Value("${telegram.commands.list}")
    private String LIST_COMMAND;

    @Value("${telegram.messages.list}")
    private String LIST_MESSAGE;

    @Autowired
    BinanceBotMainRunner binanceBotMainRunner;

    @EventListener
    public void onApplicationEvent(TelegramCommandEvent telegramCommandEvent) {
        long chatId = telegramCommandEvent.getTelegramCommandDto().getChatId();
        if (telegramCommandEvent.getTelegramCommandDto().getCommand().startsWith(LIST_COMMAND)) {
            List<String> bots = binanceBotMainRunner.getAllBean();
            telegramHelper.sendMessage(
                    String.valueOf(LIST_MESSAGE).replace("<BOT>", String.join("\n ", bots)),
                    chatId);
        }
    }

}