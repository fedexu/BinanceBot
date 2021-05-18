package com.fedexu.binancebot.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class TelegramBotMain {

    Logger logger = LoggerFactory.getLogger(TelegramBotMain.class);

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private TelegramHelper telegramHelper;

    @Value("${telegram.messages.welcome}")
    private String WELCOME_MESSAGE;

    @Value("${telegram.messages.exit}")
    private String EXIT_MESSAGE;

    @Value("${telegram.commands.start}")
    private String START_COMMAND;

    @Value("${telegram.commands.stop}")
    private String STOP_COMMAND;

    @Value("${telegram.messages.configuration}")
    private String CONFIGURATION_MESSAGE;

    public volatile boolean exitCondition;
    private boolean errorOnTelegramListener;

    //Dead man's solution
    @Scheduled(fixedDelay = Long.MAX_VALUE)
    public void run() {
        logger.info("TelegramBot started!");
        try {
            processMessages();
            while (!exitCondition) {
                if (errorOnTelegramListener) {
                    telegramBot.removeGetUpdatesListener();
                    logger.error("Error on Telegram BOT...Restarting...");
                    errorOnTelegramListener = false;
                    processMessages();
                    TimeUnit.SECONDS.sleep(15);
                }
                TimeUnit.SECONDS.sleep(15);
            }
        } catch (Exception e) {
            logger.error("exception occurs in TelegramBot Thread : ", e);
        }
    }

    public void processMessages() {
        // Register for updatesupdates
        telegramBot.setUpdatesListener(updates -> {
            updates.forEach(update -> {
                try {
                    logger.info("TelegramBot update : " + update.updateId());
                    long chatId = update.message().chat().id();
                    String text = update.message().text();

                    if (STOP_COMMAND.equals(text)) {
                        if (telegramHelper.isUserRegistered(chatId)) {
                            telegramHelper.removeUser(chatId);
                            telegramHelper.sendMessage(EXIT_MESSAGE, chatId);
                        }
                    } else if (START_COMMAND.equals(text)) {
                        if (!telegramHelper.isUserRegistered(chatId)) {
                            telegramHelper.addUser(
                                    User.builder().username(update.message().chat().username()).chatId(chatId).build());
                            telegramHelper.sendMessage(WELCOME_MESSAGE, chatId);
                        }
                    } else {
                        telegramHelper.sendMessage(CONFIGURATION_MESSAGE, chatId);
                    }
                } catch (Exception e) {
                    errorOnTelegramListener = true;
                    logger.error("exception occurs in TelegramBot Listener : ", e);
                }
            });
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });

    }

}