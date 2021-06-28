package com.fedexu.binancebot.telegram;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class TelegramHelper {

    Logger logger = LoggerFactory.getLogger(TelegramHelper.class);

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    Firestore firestore;

    @Value("${firestore.collection.telegramuser}")
    private String usersCollection;

    public void sendMessageToSubscribed(String message) {
        getAllUser().forEach(
                (aLong, user) -> {
                    try {
                        telegramBot.execute(new SendMessage(user.getChatId(), message));
                    } catch (Exception e) {
                        logger.error("error in Telegram Helper" , e);
                    }
                });
    }

    public void sendMessage(String message, long chatId) {
        telegramBot.execute(new SendMessage(chatId, message));
    }

    @SneakyThrows
    public User save(User user) {
        User addedUser;
        try {
            //ADD new User to the list
            DocumentReference docRef =
                    firestore.collection(usersCollection).document(String.valueOf(user.getChatId()));
            //asynchronously write data
            ApiFuture<WriteResult> result = docRef.set(user);
            //synchronously wait for data to be write
            result.get();
            addedUser = user;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error Firestore SAVE: " + e);
            throw e;
        }
        return addedUser;
    }

    @SneakyThrows
    public boolean remove(long chatId) {
        boolean removed;
        try {
            //Remove user to the list
            ApiFuture<WriteResult> writeResult =
                    firestore.collection(usersCollection).document(String.valueOf(chatId)).delete();
            //synchronously wait for data to be write
            writeResult.get();
            removed = true;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error Firestore DELETE: " + e);
            throw e;
        }
        return removed;
    }

    @SneakyThrows
    public Map<Long, User> getAllUser() {
        Map<Long, User> users;
        try {
            // asynchronously retrieve all users
            ApiFuture<QuerySnapshot> query = firestore.collection(usersCollection).get();
            //synchronously wait for data to be read
            QuerySnapshot querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
            users = documents.stream()
                    .collect(Collectors.toMap(
                            document -> document.getLong("chatId"),
                            this::mapToUser
                    ));
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error Firestore GET ALL: " + e);
            throw e;
        }
        return users;
    }

    @SneakyThrows
    public User find(long chatId) {
        User searchedUser = null;
        try {
            DocumentReference docRef = firestore.collection(usersCollection).document(String.valueOf(chatId));
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                searchedUser = mapToUser(document);
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error Firestore GET: " + e);
            throw e;
        }
        return searchedUser;
    }

    private User mapToUser(DocumentSnapshot document) {
        return User.builder()
                .username(document.getString("username"))
                .chatId(document.getLong("chatId"))
                .version(document.getString("version"))
                .build();
    }
}
