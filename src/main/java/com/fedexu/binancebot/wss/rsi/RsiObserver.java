package com.fedexu.binancebot.wss.rsi;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RsiObserver {

    Logger logger = LoggerFactory.getLogger(com.fedexu.binancebot.wss.macd.MacdObserver.class);

    public static double[] rsi(double[] prices, int PERIODS_AVERAGE) {
        Core taLibCore = new Core();
        double[] tempOutPut = new double[prices.length];
        double[] output = new double[prices.length];
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        begin.value = -1;
        length.value = -1;

        taLibCore.rsi(0, prices.length - 1, prices, PERIODS_AVERAGE, begin, length, tempOutPut);
        for (int i = 0; i < PERIODS_AVERAGE; i++) {
            output[i] = 0;
        }
        for (int i = PERIODS_AVERAGE; 0 < i && i < (prices.length); i++) {
            output[i] = tempOutPut[i - PERIODS_AVERAGE];
        }

        System.out.println("RSI " + PERIODS_AVERAGE + " " + output[prices.length-1]);

        return output;
    }
}
