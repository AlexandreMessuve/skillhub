package com.skillhub.util;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class MyUtil {
    /**
     * Generates a random numeric code of specified length.
     *
     * @param numberOfDigits the length of the code to be generated
     * @return a string representing the random numeric code
     */
    public String generateRandomCode(int numberOfDigits) {
        StringBuilder code = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < numberOfDigits; i++) {
            int randomDigit = random.nextInt(10); // Generates a random digit between 0 and 9
            code.append(randomDigit); // Append the random digit to the code
        }
        return code.toString();
    }
}
