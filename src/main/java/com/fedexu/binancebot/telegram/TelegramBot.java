package com.fedexu.binancebot.telegram;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import it.flp.telegram.bot.Bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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

    @Value("${telegram.messages.configuration}")
    private String CONFIGURATION_MESSAGE;

    @Value("${firestore.collection.telegramuser}")
    private String usersCollection;

    @Autowired
    Firestore firestore;

    Logger logger = LoggerFactory.getLogger(TelegramBot.class);

    Map<Long, User> registeredUserToBot = new HashMap<>();
    AtomicReference<Long> lastUpdateId = new AtomicReference<>(0L);

    private volatile boolean exitCondition;

    //Dead man's solution
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Scheduled(fixedDelay = Long.MAX_VALUE)
    public void run() {
        logger.info("TelegramBot STARTED!");
        registeredUserToBot = getAllUser();
        while (!exitCondition) {
            try {
                telegramBot.getUpdater().getUpdatesWithOffset(lastUpdateId.get())
                        .getResult().forEach(throwingConsumerWrapper(updateMessage -> {
                    if (!isNull(updateMessage.getMessage())
                            && !lastUpdateId.toString().equals(String.valueOf(updateMessage.getUpdateId()))) {

                        long chatId = updateMessage.getMessage().getSender().getId();
                        if (STOP_COMMAND.equals(updateMessage.getMessage().getText())) {
                            if (registeredUserToBot.containsKey(chatId)) {
                                //Remove user to the list
                                ApiFuture<WriteResult> writeResult =
                                        firestore.collection(usersCollection).document(String.valueOf(chatId)).delete();
                                //synchronously wait for data to be write
                                writeResult.get().getUpdateTime();
                                registeredUserToBot.remove(chatId);
                                telegramBot.getSender().sendMessage(EXIT_MESSAGE, chatId);
                            }
                        } else if (START_COMMAND.equals(updateMessage.getMessage().getText())) {
                            if (!registeredUserToBot.containsKey(chatId)) {
                                //ADD new User to the list
                                DocumentReference docRef =
                                        firestore.collection(usersCollection).document(String.valueOf(chatId));
                                User newUser = User.builder()
                                        .username(updateMessage.getMessage().getSender().getUsername())
                                        .chatId(chatId).build();
                                registeredUserToBot.put(chatId, newUser);
                                //asynchronously write data
                                ApiFuture<WriteResult> result = docRef.set(newUser);
                                //synchronously wait for data to be write
                                result.get().getUpdateTime();
                                telegramBot.getSender().sendMessage(WELCOME_MESSAGE, chatId);
                            }
                        } else {
                            telegramBot.getSender().sendMessage(CONFIGURATION_MESSAGE, chatId);
                        }
                    }
                    lastUpdateId.set(updateMessage.getUpdateId());
                }));

            } catch (Exception e) {
                logger.error("Error Occurs on Telegram Bot Runner", e);
            }
        }
    }

    public void sendMessageToSubscribed(String message) {
        registeredUserToBot = getAllUser();
        registeredUserToBot.forEach(
                (aLong, user) -> {
                    try {
                        telegramBot.getSender().sendMessage(message, user.getChatId());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private Map<Long, User> getAllUser() {
        Map<Long, User> users = null;
        try {
            // asynchronously retrieve all users
            ApiFuture<QuerySnapshot> query = firestore.collection(usersCollection).get();
            //synchronously wait for data to be read
            QuerySnapshot querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
            users = documents.stream()
                    .collect(Collectors.toMap(
                            document -> document.getLong("chatId"),
                            document -> User.builder()
                                    .id(Long.valueOf(document.getId()))
                                    .username(document.getString("username"))
                                    .chatId(document.getLong("chatId"))
                                    .build()
                    ));
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return users;
    }

}