package com.fedexu.binancebot.telegram.commands;

import com.fedexu.binancebot.telegram.TelegramHelper;
import com.fedexu.binancebot.telegram.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import static java.util.Objects.isNull;

@Service
public class Stop {

    Logger logger = LoggerFactory.getLogger(Start.class);

    @Autowired
    private TelegramHelper telegramHelper;

    @Value("${telegram.messages.exit}")
    private String EXIT_MESSAGE;

    @Value("${telegram.commands.stop}")
    private String STOP_COMMAND;

    @EventListener
    public void onApplicationEvent(TelegramCommandEvent telegramCommandEvent) {
        long chatId = telegramCommandEvent.getTelegramCommandDto().getChatId();
        if (STOP_COMMAND.equals(telegramCommandEvent.getTelegramCommandDto().getCommand())) {
            User user = telegramHelper.find(chatId);
            if (!isNull(user)) {
                telegramHelper.remove(chatId);
                telegramHelper.sendMessage(EXIT_MESSAGE, chatId);
            }
        }
    }

}