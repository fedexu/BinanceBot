package com.fedexu.binancebot.event.commands.notify;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import static com.fedexu.binancebot.event.commands.notify.NotifyType.ADD;

@Getter
@Setter
public class NotifyEvent extends ApplicationEvent {

    private NotifyType type;
    private String symbol;
    private double price;
    private Direction direction;
    private long chatId;
    private double actualPrice;

    public NotifyEvent(Object source, String symbol, double price, Direction direction , long chatId) {
        super(source);
        this.type = ADD;
        this.symbol = symbol;
        this.price = price;
        this.direction = direction;
        this.chatId = chatId;
    }
}
