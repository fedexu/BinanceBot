package com.fedexu.binancebot.event.commands;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class TelegramCommandEvent extends ApplicationEvent {

    private long eventTime;
    private TelegramCommandDto telegramCommandDto;

    public TelegramCommandEvent(Object source, TelegramCommandDto telegramCommandDto) {
        super(source);
        this.eventTime = System.currentTimeMillis();
        this.telegramCommandDto = telegramCommandDto;
    }

}
