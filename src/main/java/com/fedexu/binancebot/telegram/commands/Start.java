package com.fedexu.binancebot.telegram.commands;

import com.fedexu.binancebot.event.commands.TelegramCommandEvent;
import com.fedexu.binancebot.telegram.TelegramHelper;
import com.fedexu.binancebot.telegram.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import static java.util.Objects.isNull;

@Service
public class Start implements ApplicationListener<TelegramCommandEvent> {

    Logger logger = LoggerFactory.getLogger(Start.class);

    @Autowired
    private TelegramHelper telegramHelper;

    @Value("${telegram.commands.start}")
    private String START_COMMAND;

    @Value("${telegram.messages.welcome}")
    private String WELCOME_MESSAGE;

    @Override
    public void onApplicationEvent(TelegramCommandEvent telegramCommandEvent) {
        long chatId = telegramCommandEvent.getTelegramCommandDto().getChatId();
        if (START_COMMAND.equals(telegramCommandEvent.getTelegramCommandDto().getCommand())) {
            User user = telegramHelper.find(chatId);
            if (isNull(user)) {
                telegramHelper.save(telegramCommandEvent.getTelegramCommandDto().getUser());
                telegramHelper.sendMessage(WELCOME_MESSAGE, chatId);
            }
        }
    }

}
