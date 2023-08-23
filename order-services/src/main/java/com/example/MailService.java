package com.example;

import com.example.request.EmailRequest;

public interface MailService {
    String sendEmail(EmailRequest mailBody);
}
