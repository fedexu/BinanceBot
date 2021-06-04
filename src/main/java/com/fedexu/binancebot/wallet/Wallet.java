package com.fedexu.binancebot.wallet;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Wallet {

    //for test purpose
    private Double fiat = 1000.0;
    private Double coin = 0.0;
    private final Double fee = 1.001;
    private Double totalFee = 0.0;

    private String SYMBOL;

    Wallet (String symbol){
        this.SYMBOL = symbol;
    }

}
