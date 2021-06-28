package com.fedexu.binancebot;

import com.fedexu.binancebot.wss.BinanceBotMain;
import com.sendgrid.SendGrid;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
@AutoConfigureMockMvc
class BinanceBotApplicationTests {

    @MockBean
    private BinanceBotMain binanceBotMain;

    @Test
    void BotStarting() {
        binanceBotMain.run();
    }

}
