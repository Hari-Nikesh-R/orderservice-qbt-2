package com.example;

import com.example.request.EmailRequest;
import com.example.request.EmailTemplate;
import com.example.request.MailConfiguration;
import com.itextpdf.text.DocumentException;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Properties;

@Service
@Slf4j
public class MailServiceImpl implements MailService {
    private final Calendar calendar = Calendar.getInstance();

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public String sendEmail(EmailRequest mailBody) {
        try {
            log.info("Send email invoked");
            String filename = mailBody.getRecipient() + "-" + currentDateAsString() + "bill.pdf";
            convertByteArrayResourceToPdf(mailBody.getPdfData(), filename);
            MailConfiguration mailConfiguration = setUpMailClient();
            Message message = new MimeMessage(mailConfiguration.getSession());
            message.setFrom(new InternetAddress(mailConfiguration.getUsername()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailBody.getRecipient()));
            message.setSubject(mailBody.getSubject());
            message.setContent(attachPdfToMail(invoiceEmailTemplate(mailBody.getRecipient()), filename));
            message.saveChanges();
            Transport.send(message);
            log.info("Mail sent successfully");
            deleteFileFromDisk(filename);
            return "Mail sent successfully";
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
            exception.printStackTrace();
            return exception.getMessage();
        }
    }

    private void deleteFileFromDisk(String filename) {
        try {
            File file = new File(filename);
            if (file.exists()) {
                if (file.delete()) {
                    log.info("File deleted");
                } else {
                    log.warn("File not deleted");
                }
            }
        } catch (Exception exception) {
            log.error("File not found");
        }
    }

    private void convertByteArrayResourceToPdf(byte[] byteArrayResource, String fileName) throws IOException, DocumentException {
        OutputStream out = new FileOutputStream(fileName);
        out.write(byteArrayResource);
        out.close();
        System.out.println("PDF file generated at: " + fileName);
    }

    private Multipart attachPdfToMail(String html, String filename) {
        try {
            BodyPart messageBodyPart = new MimeBodyPart();
            Multipart multipart = new MimeMultipart();
            messageBodyPart.setText(html);
            messageBodyPart.setContent(html, "text/html; charset=utf-8");
            multipart.addBodyPart(messageBodyPart);
            messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(filename);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(filename);
            multipart.addBodyPart(messageBodyPart);
            return multipart;
        } catch (MessagingException messagingException) {
            System.out.println(messagingException.getMessage());
            messagingException.printStackTrace();
            throw new RuntimeException();
        }
    }

    private String currentDateAsString() {
        return calendar.get(Calendar.DATE) + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.YEAR);
    }

    private MailConfiguration setUpMailClient() {
        final String username = "dosmartie@gmail.com";
        final String password = "k9DGrv5ghC7SOncR";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "mail.smtp2go.com");
        props.put("mail.smtp.port", "2525");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        return new MailConfiguration(session, username);
    }

    //todo: For all kind for mail template
    private String selectEmailTemplate(EmailTemplate emailTemplate, String recipient) {
        switch (emailTemplate) {
            case INVOICE -> {
                return invoiceEmailTemplate(recipient);
            }
            case OTHER -> {
                return "<>";
            }
            default -> {
                return "";
            }
        }
    }

    private String invoiceEmailTemplate(String recipient) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Thank You</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;\">\n" +
                "        <h2>Thank You!</h2>\n" +
                "        <p>Dear " + recipient + ",</p>\n" +
                "        <p>We wanted to take a moment to express our sincere gratitude for your continued support and business. Your trust in our products/services means a lot to us.</p>\n" +
                "        <p>If you have any questions, feedback, or need assistance, please don't hesitate to reach out to us.</p>\n" +
                "        <p>Once again, thank you for choosing us.</p>\n" +
                "        <p>Find your Invoice pdf of your purchase below</p>\n" +
                "        <p>Best regards,</p>\n" +
                "        <p>Your [Blibli India] Team</p>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}
