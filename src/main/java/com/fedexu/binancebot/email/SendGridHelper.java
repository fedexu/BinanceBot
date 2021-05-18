package com.fedexu.binancebot.email;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SendGridHelper {

    Logger logger = LoggerFactory.getLogger(SendGridHelper.class);

    @Autowired
    private SendGrid sendGridClient;

    public void sendMail(String text) throws IOException {

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(text);
        Response response = sendGridClient.api(request);
        logger.info("STATUS: " + response.getStatusCode());

    }
//    example calling
//            ClassLoader classLoader = getClass().getClassLoader();
//            File file = new File(Objects.requireNonNull(classLoader.getResource("sendGridTemplate/sendGridEmailBody.json")).getFile());
//            sendGridHelper.sendMail(new String(Files.readAllBytes(file.toPath())));
//            logger.info("mail Sended");
}
