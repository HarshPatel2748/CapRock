package com.caprock.service.impl;

import com.caprock.service.EmailService;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Override
    public void sendVerificationEmail(String toEmail, String name, String code){
        Resend resend = new Resend(resendApiKey);

        String html = """
                <!DOCTYPE html>
                                <html>
                                <body style="font-family: sans-serif; background: #0a0a0f; color: #f1f0f5; padding: 40px;">
                                    <div style="max-width: 480px; margin: 0 auto; background: #111118; border-radius: 16px; border: 1px solid #ffffff12; padding: 40px;">
                                        <h1 style="font-size: 24px; font-weight: 700; margin-bottom: 8px;">Verify your email</h1>
                                        <p style="color: #9ca3af; margin-bottom: 32px;">Hi %s, enter this code to verify your CapRock account:</p>
                                        <div style="background: #1a1a24; border-radius: 12px; padding: 24px; text-align: center; margin-bottom: 32px;">
                                            <span style="font-size: 40px; font-weight: 800; letter-spacing: 12px; color: #7c3aed;">%s</span>
                                        </div>
                                        <p style="color: #6b7280; font-size: 13px;">This code expires in 10 minutes. If you didn't create a CapRock account, you can ignore this email.</p>
                                    </div>
                                </body>
                                </html>
                """.formatted(name, code);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("CapRock <onboarding@resend.dev>")
                .to(toEmail)
                .subject("Verify your CapRock account")
                .html(html)
                .build();

        try{
            resend.emails().send(params);
        } catch (ResendException e) {
            throw new RuntimeException("Failed to send verification email: " + e.getMessage());
        }
    }
}
