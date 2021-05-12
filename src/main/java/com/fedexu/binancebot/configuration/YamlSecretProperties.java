package com.fedexu.binancebot.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
@ConfigurationProperties
@PropertySource(value = "classpath:secret.yaml", factory = YamlPropertySourceFactory.class)
public class YamlSecretProperties {

    private String BINANCE_WSS;
    private String BINANCE_API_KEY;
    private String BINANCE_SECRET_KEY;
    private String SENDGRID_API_KEY;
    private String TELEGRAM_BOT_KEY;


}
