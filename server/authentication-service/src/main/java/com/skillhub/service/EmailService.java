package com.skillhub.service;

import com.skillhub.entity.UserInfo;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger LOG = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.base.url}")
    private String appBaseUrl;

    @Value("${spring.mail.from}")
    private String fromEmail;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send a welcome email to the user after registration.
     * @param user the user to whom the welcome email will be sent.
     */
    public void sendWelcomeEmail(UserInfo user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setFrom(fromEmail);
        message.setSubject("Welcome to SkillHub!");
        message.setText("Hey " + user.getFirstName() + ",\n\n" +
                "Your account has been successfully validated.\n\n" +
                "Best regards,\n" +
                "The SkillHub Team");

        mailSender.send(message);
        System.out.println("Mail sent to: " + user.getEmail());
    }

    @Async
    public void sendVerifyEmailHtml(UserInfo user) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String verificationUrl = appBaseUrl + "/api/auth/verify?token=" + user.getVerificationToken();

            String htmlMsg = "<h3>Hey " + user.getFirstName() + ",</h3><p>Please click on the link for : <a href=\"" + verificationUrl + "\">Verify my account</a></p>";

            helper.setText(htmlMsg, true);
            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("SkillHub Account Verification");

            mailSender.send(mimeMessage);
            LOG.info("Mail sent to: {}",user.getEmail());

        } catch (MessagingException e) {
            LOG.error("Error fail to sent mail: {}",e.getMessage());
        }
    }
}