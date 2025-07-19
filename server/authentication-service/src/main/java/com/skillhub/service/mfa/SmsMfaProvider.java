package com.skillhub.service.mfa;

import com.skillhub.entity.MfaMethod;
import com.skillhub.entity.UserInfo;
import com.skillhub.service.EmailSmsMfaCodeService;
import com.skillhub.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SmsMfaProvider implements MfaProvider{

    private final static Logger LOG = LoggerFactory.getLogger(SmsMfaProvider.class);

    private final EmailSmsMfaCodeService emailSmsMfaCodeService;

    private final SmsService smsService;

    /**
     * Constructs an SmsMfaProvider with the necessary services.
     *
     * @param emailSmsMfaCodeService the service for handling SMS verification codes
     * @param smsService the service for sending SMS messages
     */
    public SmsMfaProvider(EmailSmsMfaCodeService emailSmsMfaCodeService, SmsService smsService) {
        this.emailSmsMfaCodeService = emailSmsMfaCodeService;
        this.smsService = smsService;
    }
    @Override
    public Map<String, Object> initiateSetup(UserInfo userInfo) {
        sendVerificationCode(userInfo);
        return Map.of("status", "success",
                "message", "A verification code has been sent to your phone.");
    }

    /**
     * Sends a verification code to the user's phone number.
     *
     * @param userInfo the user information containing the phone number
     */
    @Override
    public void sendVerificationCode(UserInfo userInfo) {
        String code = emailSmsMfaCodeService.generateCode(userInfo.getEmail());
        smsService.sendSms(userInfo.getPhoneNumber(), "Your verification code is: " + code);
        LOG.info("Verification code sent to phone number: {}", userInfo.getEmail());
    }

    /**
     * Verifies the provided code against the stored code for the user's phone number.
     *
     * @param userInfo the user information containing the phone number
     * @param code the verification code to be verified
     * @return true if the code is valid, false otherwise
     */
    @Override
    public boolean verifyCode(UserInfo userInfo, String code) {
        return emailSmsMfaCodeService.isValidCode(userInfo.getEmail(), code);
    }

    /**
     * Returns the multi-factor authentication method for this provider.
     *
     * @return the MfaMethod representing SMS
     */
    @Override
    public MfaMethod getMethod() {
        return MfaMethod.SMS;
    }
}
