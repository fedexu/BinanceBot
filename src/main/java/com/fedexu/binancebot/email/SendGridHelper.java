package com.fedexu.binancebot.email;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class SendGridHelper {

    Logger logger = LoggerFactory.getLogger(SendGridHelper.class);

    @Autowired
    private SendGrid sendGridClient;

    @Value("${sendgrid.mail.template}")
    String emailTemplate;

    private final String TO = "<toEmail>";
    private final String SUBJECT = "<subject>";
    private final String BODY = "<body>";

    public void sendMail(List<String> to, String subject, String body) {
        try {
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(emailTemplate.replace(TO, String.join(",", to)).replace(SUBJECT, subject).replace(BODY, body));
            Response response = sendGridClient.api(request);
            logger.info("Mail sent, STATUS: " + response.getStatusCode());
        } catch (IOException e) {
            logger.error("error in SendGrid Helper" , e);
        }
    }

}
