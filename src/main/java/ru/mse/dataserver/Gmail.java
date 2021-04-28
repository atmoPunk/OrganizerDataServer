package ru.mse.dataserver;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;


//public class Gmail {
//    private static String USER_NAME = "*****";  // GMail user name (just the part before "@gmail.com")
//    private static String PASSWORD = "********"; // GMail password
//
//    private static void send(String[] to, String subject, String body) {
//        Properties props = System.getProperties();
//        String host = "smtp.gmail.com";
//        props.put("mail.smtp.starttls.enable", "true");
//        props.put("mail.smtp.host", host);
//        props.put("mail.smtp.user", USER_NAME);
//        props.put("mail.smtp.password", PASSWORD);
//        props.put("mail.smtp.port", "587");
//        props.put("mail.smtp.auth", "true");
//
//        Session session = Session.getDefaultInstance(props);
//        MimeMessage message = new MimeMessage(session);
//
//        try {
//            message.setFrom(new InternetAddress(from));
//            InternetAddress[] toAddress = new InternetAddress[to.length];
//
//            // To get the array of addresses
//            for (int i = 0; i < to.length; i++) {
//                toAddress[i] = new InternetAddress(to[i]);
//            }
//
//            for (int i = 0; i < toAddress.length; i++) {
//                message.addRecipient(Message.RecipientType.TO, toAddress[i]);
//            }
//
//            message.setSubject(subject);
//            message.setText(body);
//            Transport transport = session.getTransport("smtp");
//            transport.connect(host, from, pass);
//            transport.sendMessage(message, message.getAllRecipients());
//            transport.close();
//        } catch (AddressException ae) {
//            ae.printStackTrace();
//        } catch (MessagingException me) {
//            me.printStackTrace();
//        }
//    }
//
//
//}
