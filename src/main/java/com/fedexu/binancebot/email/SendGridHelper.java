package com.fedexu.binancebot.email;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SendGridHelper {

    Logger logger = LoggerFactory.getLogger(SendGridHelper.class);

    private SendGrid SendGridClient;

    public SendGridHelper(String ApiKey){
        SendGridClient = new SendGrid(ApiKey);
        logger.info("SendGridClient Configured!");
    }

    public void sendMail(String text) throws IOException{

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(text);
        Response response = SendGridClient.api(request);
        logger.info("STATUS: " + response.getStatusCode());

    }
}
