package com.fedexu.binancebot.telegram.commands;

import com.fedexu.binancebot.event.commands.TelegramCommandEvent;
import com.fedexu.binancebot.telegram.TelegramHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class Notify {

    Logger logger = LoggerFactory.getLogger(Notify.class);

    @Value("${telegram.commands.notify}")
    private String NOTIFY_COMMAND;

    @Value("${telegram.messages.notify}")
    private String NOTIFY_MESSAGE;

    @Autowired
    private TelegramHelper telegramHelper;

    @EventListener
    public void onApplicationEvent(TelegramCommandEvent telegramCommandEvent) {
        long chatId = telegramCommandEvent.getTelegramCommandDto().getChatId();
        if (telegramCommandEvent.getTelegramCommandDto().getCommand().startsWith(NOTIFY_COMMAND)) {
            String[] args = telegramCommandEvent.getTelegramCommandDto().getCommand()
                    .replace(NOTIFY_COMMAND + " ", "").split(" ");

            telegramHelper.sendMessage(NOTIFY_MESSAGE, chatId);
        }
    }

}
