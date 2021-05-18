package com.fedexu.binancebot.event.commands;

import com.fedexu.binancebot.event.order.OrderStatusDto;
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
