package com.fedexu.binancebot.wss;

import com.binance.api.client.domain.market.CandlestickInterval;
import lombok.Builder;
import lombok.Data;

import java.io.IOException;

@Data
@Builder
public class CandelStickTimesFrame {

    private long start;
    private long end;

    public static CandelStickTimesFrame calculateCandlestickTimesFrame(CandlestickInterval interval, int PERIODS_AVERAGE) throws IOException {
        long timeToSubstract = 60 * 1000;
        switch (interval) {
            case ONE_MINUTE:
            case THREE_MINUTES:
            case FIVE_MINUTES:
            case FIFTEEN_MINUTES:
            case HALF_HOURLY:
                timeToSubstract = timeToSubstract * Long.parseLong(interval.getIntervalId().replace("m", ""));
                break;
            case HOURLY:
            case TWO_HOURLY:
            case FOUR_HOURLY:
            case SIX_HOURLY:
            case EIGHT_HOURLY:
            case TWELVE_HOURLY:
                timeToSubstract = timeToSubstract * 60 * Long.parseLong(interval.getIntervalId().replace("h", ""));
                break;
            case DAILY:
            case THREE_DAILY:
                timeToSubstract = timeToSubstract * 60 * 24 * Long.parseLong(interval.getIntervalId().replace("d", ""));
                break;
            case WEEKLY:
                timeToSubstract = timeToSubstract * 60 * 24 * 7 * Long.parseLong(interval.getIntervalId().replace("w", ""));
                break;
            default:
                throw new IOException("Interval value not correct");
        }

        return CandelStickTimesFrame.builder()
                .start(System.currentTimeMillis() - ( timeToSubstract * PERIODS_AVERAGE) )
                .end(System.currentTimeMillis())
                .build();
    }

}
