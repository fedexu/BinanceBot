package com.fedexu.binancebot.wss.talib;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MacdValues {
    double macd;
    double signal;
    double hist;
}