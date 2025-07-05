package com.skillhub.service.mfa;

import com.skillhub.entity.MfaMethod;
import com.skillhub.entity.UserInfo;
import com.skillhub.service.EmailService;
import com.skillhub.service.EmailVerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EmailMfaProvider implements MfaProvider {
    private final EmailVerificationCodeService emailVerificationCodeService;

    private final EmailService emailService;



    /**
     * Constructs an EmailMfaProvider with the necessary services.
     *
     * @param emailVerificationCodeService the service for handling email verification codes
     * @param emailService the service for sending emails
     */
    @Autowired
    public EmailMfaProvider(EmailVerificationCodeService emailVerificationCodeService, EmailService emailService) {
        this.emailVerificationCodeService = emailVerificationCodeService;
        this.emailService = emailService;
    }

    /**
     * Initiates the setup process for email-based multi-factor authentication.
     *
     * @param userInfo the user information containing the email address
     * @return a map containing the status and message of the setup initiation
     */
    @Override
    public Map<String, Object> initiateSetup(UserInfo userInfo) {
        if (!userInfo.isVerified()){
            throw new IllegalStateException("User is not verified");
        }

        sendVerificationCode(userInfo);
        return Map.of(
                "status", "success",
                "message", "Email MFA setup initiated. A verification code has been sent to your email."
        );
    }

    /**
     * Sends a verification code to the user's email.
     *
     * @param userInfo the user information containing the email address
     */
    @Override
    public void sendVerificationCode(UserInfo userInfo) {
        String email = userInfo.getEmail();
        String code = emailVerificationCodeService.generateCode(email);
        emailService.send2faCodeEmail(userInfo, code);
    }

    /**
     * Verifies the provided code against the user's information.
     *
     * @param userInfo the user information
     * @param code the verification code to check
     * @return true if the code is valid, false otherwise
     */
    @Override
    public boolean verifyCode(UserInfo userInfo, String code) {
        return emailVerificationCodeService.isValidCode(userInfo.getEmail(), code);
    }

    /**
     * Returns the method of multi-factor authentication used by this provider.
     *
     * @return the MfaMethod associated with this provider
     */
    @Override
    public MfaMethod getMethod() {
        return MfaMethod.EMAIL;
    }
}
