package com.skillhub.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class PreAuthTokenService {
    private final Cache<String, String> tokenCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public String generateToken(String email) {
        String token = java.util.UUID.randomUUID().toString();
        tokenCache.put(email, token);
        return token;
    }

    public boolean isValidToken(String email, String token) {
        String cachedToken = tokenCache.getIfPresent(email);
        if (token.equals(cachedToken)){
            tokenCache.invalidate(email);
            return true;
        }
        return false;
    }
}
