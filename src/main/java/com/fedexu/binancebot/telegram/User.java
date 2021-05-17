package com.fedexu.binancebot.telegram;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {

    private Long id;
    private Long chatId;
    private String username;

}
