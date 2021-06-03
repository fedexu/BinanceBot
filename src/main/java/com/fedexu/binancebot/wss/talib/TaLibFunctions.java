package com.fedexu.binancebot.wss.talib;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;
import lombok.Builder;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
public class TaLibFunctions {

    private Core taLibCore = new Core();

    public double[] ema(double[] prices, int PERIODS_AVERAGE) {
        double[] tempOutPut = new double[prices.length];
        double[] output = new double[prices.length];
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        begin.value = -1;
        length.value = -1;
        taLibCore.ema(0, prices.length - 1, prices, PERIODS_AVERAGE, begin, length, tempOutPut);
        if (output.length >= PERIODS_AVERAGE) {
            for (int i = 0; i < PERIODS_AVERAGE - 1; i++) {
                output[i] = 0;
            }
            for (int i = PERIODS_AVERAGE - 1; 0 < i && i < (prices.length); i++) {
                output[i] = tempOutPut[i - PERIODS_AVERAGE + 1];
            }
        }
        return output;
    }

    public double[] kama(double[] closePrices, int PERIODS_AVERAGE) {
        double[] output = new double[closePrices.length];
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        taLibCore.movingAverage(0, closePrices.length - 1, closePrices, PERIODS_AVERAGE, MAType.Kama, begin, length, output);
        return output;
    }

    public double[] rsi(double[] prices, int PERIODS_AVERAGE) {
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

        return output;
    }

    public MacdValues macd(double[] prices, int FAST_PERIOD, int SLOW_PERIOD, int SIGNAL_PERIOD) {
        double[] tempOutPut = new double[prices.length];
        double[] output = new double[prices.length];
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        begin.value = -1;
        length.value = -1;

        int lookback;
        double[] macd = new double[prices.length];
        double[] signal = new double[prices.length];
        double[] hist = new double[prices.length];

        lookback = taLibCore.macdLookback(FAST_PERIOD, SLOW_PERIOD, SIGNAL_PERIOD);
        taLibCore.macd(0, prices.length - 1, prices, FAST_PERIOD, SLOW_PERIOD, SIGNAL_PERIOD, begin, length, macd, signal, hist);

        return MacdValues.builder().macd(macd[prices.length -1 - lookback]).signal(signal[prices.length -1 - lookback]).hist(hist[prices.length -1 - lookback]).build();
    }


}
