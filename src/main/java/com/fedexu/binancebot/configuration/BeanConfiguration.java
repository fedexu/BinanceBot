package com.fedexu.binancebot.configuration;

import com.fedexu.binancebot.email.SendGridHelper;
import com.fedexu.binancebot.telegram.TelegramBot;
import com.fedexu.binancebot.wss.BinanceWebSocketReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@AutoConfigureAfter(value = YamlSecretProperties.class)
public class BeanConfiguration {

    @Autowired
    private YamlSecretProperties yamlSecretProperties;

    @Bean
    public SendGridHelper sendGridHelper() {
        return new SendGridHelper(yamlSecretProperties.getSENDGRID_API_KEY());
    }

    @Bean
    public BinanceWebSocketReader binanceWebSocketReader() {
        return new BinanceWebSocketReader(yamlSecretProperties.getBINANCE_API_KEY(),yamlSecretProperties.getBINANCE_SECRET_KEY() );
    }

    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot(yamlSecretProperties.getTELEGRAM_BOT_KEY());
    }

}
