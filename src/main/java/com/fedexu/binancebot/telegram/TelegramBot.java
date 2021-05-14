package com.fedexu.binancebot.telegram;

import it.flp.telegram.bot.Bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.fedexu.binancebot.configuration.exception.LambdaExceptionWrappers.throwingConsumerWrapper;
import static java.util.Objects.isNull;

@Component
public class TelegramBot {

    @Autowired
    private Bot telegramBot;

    @Value("${telegram.messages.welcome}")
    private String WELCOME_MESSAGE;

    @Value("${telegram.messages.exit}")
    private String EXIT_MESSAGE;

    @Value("${telegram.commands.start}")
    private String START_COMMAND;

    @Value("${telegram.commands.stop}")
    private String STOP_COMMAND;

    Logger logger = LoggerFactory.getLogger(TelegramBot.class);

    List<Long> registeredUserToBot = new ArrayList<>();
    AtomicReference<Long> lastUpdateId = new AtomicReference<>(0L);

    private volatile boolean exitCondition;

    //    Dead man's solution
    @Scheduled(fixedDelay = Long.MAX_VALUE)
    public void run() {
        logger.info("TelegramBot STARTED!");
        logger.info(this.toString());
        try {
            while (!exitCondition) {
                telegramBot.getUpdater().getUpdatesWithOffset(lastUpdateId.get())
                        .getResult().forEach(throwingConsumerWrapper(updateMessage -> {
                    if (!isNull(updateMessage.getMessage())
                            && !lastUpdateId.toString().equals(String.valueOf(updateMessage.getUpdateId()))) {

                        if (STOP_COMMAND.equals(updateMessage.getMessage().getText())) {
                            registeredUserToBot.remove(updateMessage.getMessage().getSender().getId());
                            telegramBot.getSender().sendMessage(EXIT_MESSAGE, updateMessage.getMessage().getSender().getId());
                        }

                        if (START_COMMAND.equals(updateMessage.getMessage().getText())) {
                            registeredUserToBot.add(updateMessage.getMessage().getSender().getId());
                            telegramBot.getSender().sendMessage(WELCOME_MESSAGE, updateMessage.getMessage().getSender().getId());
                        }

                    }

                    lastUpdateId.set(updateMessage.getUpdateId());
                }));


                registeredUserToBot.forEach(user -> {
//                    telegramBot.getSender().sendMessage("ciao", user);
                });


            }
        } catch (Exception e) {
            logger.error("Error Occurs on Telegram Bot Runner", e);
        }
    }

    public void sendMessageToSubscribed(String message) {
        registeredUserToBot.forEach(throwingConsumerWrapper(user -> telegramBot.getSender().sendMessage(message, user)));

    }

}