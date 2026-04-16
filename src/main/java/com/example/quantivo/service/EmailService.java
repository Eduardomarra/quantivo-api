package com.example.quantivo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendPasswordResetEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Recuperação de senha - Suporte Quantivo");
        message.setText("Clique no link abaixo para redefinir sua senha:\n"
                + "http://localhost:4200/reset-password?token=" + token);
        mailSender.send(message);
    }
}
