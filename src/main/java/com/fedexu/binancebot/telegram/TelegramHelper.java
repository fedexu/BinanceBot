package com.fedexu.binancebot.telegram;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
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
                        e.printStackTrace();
                    }
                });
    }

    public void sendMessage(String message, long chatId) {
        telegramBot.execute(new SendMessage(chatId, message));
    }

    public User save(User user) {
        User addedUser = null;
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
        }
        return addedUser;
    }

    public boolean remove(long chatId) {
        boolean removed = false;
        try {
            //Remove user to the list
            ApiFuture<WriteResult> writeResult =
                    firestore.collection(usersCollection).document(String.valueOf(chatId)).delete();
            //synchronously wait for data to be write
            writeResult.get();
            removed = true;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error Firestore DELETE: " + e);
        }
        return removed;
    }

    public Map<Long, User> getAllUser() {
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
                            this::mapToUser
                    ));
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error Firestore GET ALL: " + e);
        }
        return users;
    }

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
