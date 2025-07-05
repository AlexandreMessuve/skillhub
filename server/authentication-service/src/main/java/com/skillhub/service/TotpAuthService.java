package com.skillhub.service;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;
@Service
public class TotpAuthService {

    private static final Logger LOG = LoggerFactory.getLogger(TotpAuthService.class);

    /**
     * Generates a new TOTP secret.
     *
     * @return A new TOTP secret as a String.
     */
    public String generateNewSecret(){
        SecretGenerator secretGenerator = new DefaultSecretGenerator();
        return secretGenerator.generate();
    }

    /**
     * Generates a QR code URI for the given TOTP secret and email.
     *
     * @param secret The TOTP secret.
     * @param email The user's email address.
     * @return A data URI containing the QR code image.
     * @throws RuntimeException if there is an error generating the QR code.
     */
    public String getQrCodeUri(String secret, String email) throws RuntimeException{
        QrData data = new QrData.Builder()
                .label(email)
                .secret(secret)
                .issuer("SkillHub")
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        QrGenerator qrGenerator = new ZxingPngQrGenerator();
        try {
            byte[] qrCode = qrGenerator.generate(data);
            return getDataUriForImage(qrCode, qrGenerator.getImageMimeType());
        } catch (QrGenerationException e) {
            LOG.error("Error generating QR code for secret: {}", secret, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Validates a TOTP code against the provided secret.
     *
     * @param secret The TOTP secret.
     * @param code The TOTP code to validate.
     * @return true if the code is valid, false otherwise.
     */
    public boolean isValidCode(String secret, String code) {
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1, 6);
        DefaultCodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        verifier.setAllowedTimePeriodDiscrepancy(1);

        LOG.info("Code validation for secret {}", secret);
        return verifier.isValidCode(secret, code);
    }
}
