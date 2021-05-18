package com.fedexu.binancebot.configuration;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.Lists;
import com.pengrad.telegrambot.TelegramBot;
import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

@Component
@AutoConfigureAfter(value = YamlSecretProperties.class)
public class BeanConfiguration {

    @Autowired
    private YamlSecretProperties yamlSecretProperties;

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(3);
        return threadPoolTaskScheduler;
    }

    @Bean
    public SendGrid SendGrid() {
        return new SendGrid(yamlSecretProperties.getSENDGRID_API_KEY());
    }

    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot(yamlSecretProperties.getTELEGRAM_BOT_KEY());
    }

    @Bean
    public BinanceApiRestClient binanceApiRestClient() {
        BinanceApiClientFactory factory = BinanceApiClientFactory
                .newInstance(yamlSecretProperties.getBINANCE_API_KEY(), yamlSecretProperties.getBINANCE_SECRET_KEY());
        return factory.newRestClient();
    }

    @Bean
    public BinanceApiWebSocketClient binanceApiWebSocketClient() {
        BinanceApiClientFactory factory = BinanceApiClientFactory
                .newInstance(yamlSecretProperties.getBINANCE_API_KEY(), yamlSecretProperties.getBINANCE_SECRET_KEY());
        return factory.newWebSocketClient();
    }

    @Bean
    public Firestore firestore() {
        GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(yamlSecretProperties.getFIREBASE_TOKEN(), null))
                .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        FirestoreOptions firestoreOptions =
                FirestoreOptions.getDefaultInstance().toBuilder()
                        .setProjectId(yamlSecretProperties.getFIREBASE_PROJECT_ID())
                        .setCredentials(credentials)
                        .build();
        return firestoreOptions.getService();
    }

}
