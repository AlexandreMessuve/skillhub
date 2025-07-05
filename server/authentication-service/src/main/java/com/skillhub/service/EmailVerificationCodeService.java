package com.skillhub.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.skillhub.util.MyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class EmailVerificationCodeService {
    /**
     * This service generates and validates verification codes for email addresses.
     */
    private final Cache<String, String> codeCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    private final MyUtil myUtil;


    /**
     * Constructor for EmailVerificationCodeService.
     *
     * @param myUtil utility class for generating random codes
     */
    @Autowired
    public EmailVerificationCodeService(MyUtil myUtil) {
        this.myUtil = myUtil;
    }

    /**
     * Generates a unique verification code for the given email address.
     * The code is stored in a cache with a 10-minute expiration time.
     *
     * @param email the email address for which to generate the code
     * @return the generated verification code
     */
    public String generateCode(String email) {
        String code = myUtil.generateRandomCode(6);
        codeCache.put(email, code);
        return code;
    }

    /**
     * Validates the provided verification code against the cached code for the given email address.
     * If the code is valid, it is removed from the cache.
     *
     * @param email the email address to validate the code against
     * @param code  the verification code to validate
     * @return true if the code is valid, false otherwise
     */
    public boolean isValidCode(String email, String code) {
        String cachedCode = codeCache.getIfPresent(email);
        if (code.equals(cachedCode)){
            codeCache.invalidate(email);
            return true;
        }
        return false;
    }
}
