package ru.mse.dataserver;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.util.Properties;


public class Gmail {
    public static void send(String to, String subject, String filePath, String from, String pass) {
        Properties props = System.getProperties();
        String host = "smtp.gmail.com";
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.user", from);
        props.put("mail.smtp.password", pass);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, pass);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            InternetAddress toAddress = new InternetAddress(to);
            message.addRecipient(Message.RecipientType.TO, toAddress);

            message.setSubject(subject);
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText("text");
            MimeBodyPart filePart = new MimeBodyPart();
            try {
                filePart.attachFile(new File(filePath));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            Multipart mp = new MimeMultipart();
            mp.addBodyPart(textPart);
            mp.addBodyPart(filePart);
            message.setContent(mp);
            Transport transport = session.getTransport("smtp");

            transport.connect(host, from, pass);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        } catch (MessagingException ae) {
            ae.printStackTrace();
        }
    }
}
