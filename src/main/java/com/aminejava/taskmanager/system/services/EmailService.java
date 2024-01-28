package com.aminejava.taskmanager.system.services;

import com.aminejava.taskmanager.controller.tool.AppTool;
import com.aminejava.taskmanager.exception.GlobalException;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.ZonedDateTime;
import java.util.Properties;

@Service
@Slf4j
public class EmailService {

    private final EncryptionService encryptionService;
    private final AppTool appTool;
    @Value("${mail.smtp.host}")
    private String host;
    @Value("${mail.smtp.port}")
    private String port;
    @Value("${mail.smtp.username}")
    private String username;
    @Value("${mail.smtp.password}")
    private String password;

    public EmailService(EncryptionService encryptionService, AppTool appTool) {
        this.encryptionService = encryptionService;
        this.appTool = appTool;
    }

    private String sendEncryptUrl(String email, String username) {
        ZonedDateTime now = appTool.nowTime();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("email",email);
        jsonObject.put("username",username);
        jsonObject.put("now",now);
        return encryptionService.encryptUserData(jsonObject.toString());
    }

    public void sendEmailToUser(String email, String username, String basePath) {
        Session session = Session.getDefaultInstance(configEmailServer(),
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("testservertestserver2023@gmail.com", "hbnsqsgbbwfsptlp");
                    }
                });


        try {
            MimeMessage message = new MimeMessage(session);

            // Set From:header field of the header
            message.setFrom(new InternetAddress(host));
            message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            if(!Strings.isNullOrEmpty(basePath)){
                log.info("Activation of Task Manager Software Account of user: "+username);
                message.setSubject("Activation of Task Manager Software Account");
                String link = basePath + "/" + sendEncryptUrl(email, username);
                message.setText("Welcome: " + username + " please click this url to get you account active:  " + link);
            }
            else{
                log.info("After 24 Hours of Expiration, the user:  "+username+" is again active. ");
                message.setSubject("Enable the Account");
                message.setText("Your Account is active again ");
            }

            Transport.send(message);
        } catch (MessagingException e) {
            throw new GlobalException(e.getMessage());
        }
    }

    // configure properties
    private Properties configEmailServer() {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", true);
        properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        return properties;
    }
}
