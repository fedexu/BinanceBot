package com.fedexu.binancebot.wss.macd;

import com.fedexu.binancebot.wss.ema.EmaObserver;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MacdObserver {
    Logger logger = LoggerFactory.getLogger(MacdObserver.class);

    public static double[] macd(double[] prices, int PERIODS_AVERAGE) {
        Core taLibCore = new Core();
        double[] tempOutPut = new double[prices.length];
        double[] output = new double[prices.length];
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        begin.value = -1;
        length.value = -1;

        int lookback;
        double macd[]   = new double[prices.length];
        double signal[] = new double[prices.length];
        double hist[]   = new double[prices.length];

        lookback = taLibCore.macdLookback(12,26,9);
        taLibCore.macd(0,prices.length-1,prices,12,26,9,begin,length,macd,signal,hist);

        //System.out.println("macd : "+ macd[266] + "signal : " + signal[266] + "hist : "+ hist[266]  );
        //System.out.println(lookback);
        return output;
    }

}
