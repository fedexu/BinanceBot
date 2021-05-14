package com.fedexu.binancebot.configuration;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.fedexu.binancebot.email.SendGridHelper;
import it.flp.telegram.bot.Bot;
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
        threadPoolTaskScheduler.setPoolSize(2);
        return threadPoolTaskScheduler;
    }

    @Bean
    public SendGridHelper sendGridHelper() {
        return new SendGridHelper(yamlSecretProperties.getSENDGRID_API_KEY());
    }

    @Bean
    public Bot bot() {
        return new Bot(yamlSecretProperties.getTELEGRAM_BOT_KEY());
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

}
