package com.skillhub.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class PreAuthTokenService {
    /**
     * A simple in-memory cache to store tokens with a 5-minute expiration.
     * In a production application, consider using a distributed cache like Redis.
     */
    private final Cache<String, String> tokenCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    /**
     * Generates a unique token for the given email and stores it in the cache.
     *
     * @param email The email address for which to generate the token.
     * @return The generated token.
     */
    public String generateToken(String email) {
        String token = java.util.UUID.randomUUID().toString();
        tokenCache.put(email, token);
        return token;
    }

    /**
     * Validates the token for the given email. If valid, the token is invalidated.
     *
     * @param email The email address associated with the token.
     * @param token The token to validate.
     * @return true if the token is valid and was successfully invalidated, false otherwise.
     */
    public boolean isValidToken(String email, String token) {
        String cachedToken = tokenCache.getIfPresent(email);
        if (token.equals(cachedToken)){
            tokenCache.invalidate(email);
            return true;
        }
        return false;
    }
}
