package com.fedexu.binancebot.telegram.commands;

import com.fedexu.binancebot.telegram.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TelegramCommandDto {

    private String command;
    private User user;
    private long chatId;

}
