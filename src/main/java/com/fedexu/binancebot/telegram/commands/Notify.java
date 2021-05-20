package com.fedexu.binancebot.telegram.commands;

import com.fedexu.binancebot.event.commands.TelegramCommandEvent;
import com.fedexu.binancebot.event.commands.notify.NotifyEvent;
import com.fedexu.binancebot.telegram.TelegramHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import static com.fedexu.binancebot.event.commands.notify.Direction.from;
import static com.fedexu.binancebot.event.commands.notify.NotifyType.REMOVE;
import static java.lang.Long.parseLong;

@Service
public class Notify {

    Logger logger = LoggerFactory.getLogger(Notify.class);

    @Value("${telegram.commands.notify}")
    private String NOTIFY_COMMAND;

    @Value("${telegram.messages.notify}")
    private String NOTIFY_MESSAGE;

    @Value("${telegram.messages.price}")
    private String PRICE_MESSAGE;

    @Autowired
    private TelegramHelper telegramHelper;

    @Autowired
    private ApplicationEventPublisher publisher;

    @EventListener
    public void onApplicationEvent(TelegramCommandEvent telegramCommandEvent) {
        long chatId = telegramCommandEvent.getTelegramCommandDto().getChatId();
        if (telegramCommandEvent.getTelegramCommandDto().getCommand().startsWith(NOTIFY_COMMAND)) {
            String[] args = telegramCommandEvent.getTelegramCommandDto().getCommand()
                    .replace(NOTIFY_COMMAND + " ", "").split(" ");
            publisher.publishEvent(new NotifyEvent(this, args[0], parseLong(args[1]), from(args[2]), chatId));
            telegramHelper.sendMessage(NOTIFY_MESSAGE, chatId);
        }
    }

    @EventListener
    public void onNotifyEventEvent(NotifyEvent notifyEvent) {
        if (notifyEvent.getType() == REMOVE) {
            final String SYMBOL = "<symbol>";
            final String DIRECTION = "<direction>";
            final String PRICE = "<price>";
            final String ACTUAL_PRICE = "<actualPrice>";
            telegramHelper.sendMessage(PRICE_MESSAGE
                    .replace(SYMBOL, notifyEvent.getSymbol())
                    .replace(DIRECTION, notifyEvent.getDirection().getValueId())
                    .replace(PRICE, String.valueOf(notifyEvent.getPrice()))
                    .replace(ACTUAL_PRICE, String.valueOf(notifyEvent.getActualPrice())), notifyEvent.getChatId());
        }
    }

}
