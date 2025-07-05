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

    /**
     * Constructor for EmailService.
     * @param mailSender the JavaMailSender to be used for sending emails.
     */
    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Log the success of sending an email.
     * @param email the email address to which the mail was sent.
     */
    public void sendSuccessMailLog(String email) {
        LOG.info("Mail sent to: {}", email);
    }

    /**
     * Log the failure of sending an email.
     * @param message the error message describing the failure.
     */
    public void sendFailMail(String message) {
        LOG.error("Error fail to sent mail: {}", message);
    }
    /**
     * Send a welcome email to the user after registration.
     * @param user the user to whom the welcome email will be sent.
     */
    @Async
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

    /**
     * Send a verification email to the user after registration.
     * @param user the user to whom the verification email will be sent.
     */
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
            sendSuccessMailLog(user.getEmail());

        } catch (MessagingException e) {
            sendFailMail(e.getMessage());
        }
    }

    /**
     * Send a 2fa code email to the user.
     * @param user the user to whom the verification code email will be sent.
     * @param code the code to be included in the email for 2fa.
     */
    public void send2faCodeEmail(UserInfo user, String code) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            String htmlMsg = "<h3>Hey " + user.getFirstName() + ",</h3><p>Your verification code is: <strong>" + code + "</strong> expire in 10 minutes</p>";

            helper.setTo(user.getEmail());
            helper.setFrom(fromEmail);
            helper.setSubject("SkillHub Account 2fa Code");
            helper.setText(htmlMsg, true);

            mailSender.send(mimeMessage);
            sendSuccessMailLog(user.getEmail());
        }catch (MessagingException e){
            sendFailMail(e.getMessage());
        }


    }
}