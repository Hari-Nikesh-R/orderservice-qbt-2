package com.example.request;

import jakarta.mail.Session;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MailConfiguration {
    private Session session;
    private String username;
}
