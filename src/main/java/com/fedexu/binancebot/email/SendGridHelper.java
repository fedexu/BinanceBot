package com.fedexu.binancebot.email;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

@Service
public class SendGridHelper {

    Logger logger = LoggerFactory.getLogger(SendGridHelper.class);

    @Autowired
    private SendGrid sendGridClient;

    private final String TO = "<toEmail>";
    private final String SUBJECT = "<subject>";
    private final String BODY = "<body>";

    public void sendMail(List<String> to, String subject, String body) {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(Objects.requireNonNull(classLoader.getResource("sendGridTemplate/sendGridEmailBody.json")).getFile());
            String template = null;
            template = new String(Files.readAllBytes(file.toPath()));

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(template.replace(TO, String.join(",", to)).replace(SUBJECT, subject).replace(BODY, body));
            Response response = sendGridClient.api(request);
            logger.info("mail Sended");
            logger.info("STATUS: " + response.getStatusCode());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
