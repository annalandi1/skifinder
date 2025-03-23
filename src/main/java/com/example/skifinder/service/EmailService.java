package com.example.skifinder.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class EmailService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    public void sendEmail(String to, String subject, String content) throws IOException {
        Email from = new Email("tuaemail@gmail.com"); // Cambia con la tua email verificata su SendGrid
        Email recipient = new Email(to);
        Content emailContent = new Content("text/html", content);
        Mail mail = new Mail(from, subject, recipient, emailContent);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println("Email inviata, status code: " + response.getStatusCode());
        } catch (IOException ex) {
            throw new RuntimeException("Errore nell'invio della mail: " + ex.getMessage());
        }
    }
}
