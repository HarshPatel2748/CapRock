package com.caprock.service;

public interface EmailService {

    void sendVerificationEmail(String toEmail, String name, String code);
}
