package com.fedexu.binancebot.telegram;

import com.fedexu.binancebot.configuration.YamlSecretProperties;
import com.fedexu.binancebot.email.SendGridHelper;
import com.fedexu.binancebot.event.commands.TelegramCommandDto;
import com.fedexu.binancebot.event.commands.TelegramCommandEvent;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

@Component
public class TelegramBotMain implements ApplicationEventPublisherAware {

    Logger logger = LoggerFactory.getLogger(TelegramBotMain.class);
    private ApplicationEventPublisher publisher;

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private TelegramHelper telegramHelper;

    @Value("${telegram.messages.configuration}")
    private String CONFIGURATION_MESSAGE;

    @Value("${build.version}")
    private String buildVersion;

    @SuppressWarnings("NullableProblems")
    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    //Dead man's solution
    @Scheduled(fixedDelay = Long.MAX_VALUE)
    public void run() {
        logger.info("TelegramBot started!");
        updateVersionWelcome();
        processMessages();
    }

    public void processMessages() {
        // Register for updatesupdates
        telegramBot.setUpdatesListener(updates -> {
            updates.forEach(update -> {
                try {
                    if (!isNull(update) && !isNull(update.message())) {
                        logger.info("TelegramBot update : " + update.updateId());
                        long chatId = update.message().chat().id();
                        String text = update.message().text();
                        User user = User.builder().username(update.message().chat().username()).chatId(chatId).build();
                        if (!isNull(text) && text.startsWith("/")) {
                            publishEevent(update.message().chat().id(), update.message().text(), user);
                        } else {
                            telegramHelper.sendMessage(CONFIGURATION_MESSAGE, chatId);
                        }
                    }
                } catch (Exception e) {
                    logger.error("exception occurs in TelegramBot Listener : ", e);
                }
            });
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void publishEevent(long chatId, String text, User user) {
        publisher.publishEvent(new TelegramCommandEvent(this,
                TelegramCommandDto.builder().chatId(chatId).command(text).user(user).build()
        ));

    }

    private void updateVersionWelcome() {
        DefaultArtifactVersion version = new DefaultArtifactVersion(buildVersion);
        String newVersion = "New version is up! " + buildVersion + " ! \n ";
        telegramHelper.getAllUser().forEach((chatId, user) -> {
            if (isNull(user.getVersion()) || user.getVersion().isEmpty() ||
                    version.compareTo(new DefaultArtifactVersion(user.getVersion())) > 0) {
                telegramHelper.sendMessage(newVersion + CONFIGURATION_MESSAGE, chatId);
                user.setVersion(buildVersion);
                telegramHelper.save(user);
            }
        });
    }

}