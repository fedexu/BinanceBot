package com.fedexu.binancebot.wss.ema;

import com.binance.api.client.domain.market.CandlestickInterval;
import lombok.Builder;
import lombok.Data;

import java.io.IOException;

import static java.lang.Long.parseLong;

@Data
@Builder
public class CandelStickTimesFrame {

    private long start;
    private long end;

    public static CandelStickTimesFrame calculateCandlestickTimesFrame(CandlestickInterval interval, int PERIODS_AVERAGE) throws IOException {
        long timeToSubstract = secondsInTimeFrame(interval);

        return CandelStickTimesFrame.builder()
                .start(System.currentTimeMillis() - (timeToSubstract * PERIODS_AVERAGE))
                .end(System.currentTimeMillis())
                .build();
    }

    public static long secondsInTimeFrame(CandlestickInterval interval) throws IOException {
        long seconds = 60 * 1000;
        switch (interval) {
            case ONE_MINUTE:
            case THREE_MINUTES:
            case FIVE_MINUTES:
            case FIFTEEN_MINUTES:
            case HALF_HOURLY:
                seconds = seconds * parseLong(interval.getIntervalId().replace("m", ""));
                break;
            case HOURLY:
            case TWO_HOURLY:
            case FOUR_HOURLY:
            case SIX_HOURLY:
            case EIGHT_HOURLY:
            case TWELVE_HOURLY:
                seconds = seconds * 60 * parseLong(interval.getIntervalId().replace("h", ""));
                break;
            case DAILY:
            case THREE_DAILY:
                seconds = seconds * 60 * 24 * parseLong(interval.getIntervalId().replace("d", ""));
                break;
            case WEEKLY:
                seconds = seconds * 60 * 24 * 7 * parseLong(interval.getIntervalId().replace("w", ""));
                break;
            default:
                throw new IOException("Interval value not correct");
        }
        return seconds;
    }

}
