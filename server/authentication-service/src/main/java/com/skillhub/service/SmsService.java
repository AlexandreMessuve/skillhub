package com.skillhub.service;

import com.twilio.Twilio;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    private final static Logger LOG = LoggerFactory.getLogger(SmsService.class);

    @Value("${twilio.account-sid}")
    private String twilioAccountSid;

    @Value("${twilio.auth-token}")
    private String twilioAuthToken;

    @Value("${twilio.phone-number}")
    private String fromPhoneNumber;

    /**
     * Initializes the Twilio client with the account SID and auth token.
     */
    @PostConstruct
    public void init(){
        Twilio.init(twilioAccountSid, twilioAuthToken);
        LOG.info("Twilio initialized with Account SID: {}", twilioAccountSid);
    }

    /**
     * Sends an SMS message to the specified phone number.
     *
     * @param to      The recipient's phone number.
     * @param message The message to be sent.
     */
    public void sendSms(String to, String message) {
        try {
            com.twilio.rest.api.v2010.account.Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(fromPhoneNumber),
                    message
            ).create();
            LOG.info("SMS sent to: {}", to);
        } catch (Exception e) {
            LOG.error("Failed to send SMS to {}: {}", to, e.getMessage());
        }
    }
}
