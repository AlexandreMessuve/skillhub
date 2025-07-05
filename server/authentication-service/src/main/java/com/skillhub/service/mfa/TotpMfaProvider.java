package com.skillhub.service.mfa;

import com.skillhub.entity.MfaMethod;
import com.skillhub.entity.UserInfo;
import com.skillhub.service.TotpAuthService;
import com.skillhub.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TotpMfaProvider implements MfaProvider{
    private final TotpAuthService totpAuthService;

    /**
     * Constructor for TotpMfaProvider.
     *
     * @param totpAuthService The service responsible for TOTP authentication operations.
     */
    @Autowired
    public TotpMfaProvider(TotpAuthService totpAuthService) {
        this.totpAuthService = totpAuthService;
    }

    /**
     * Initiates the setup process for TOTP MFA by generating a new secret and returning the QR code URI.
     *
     * @param userInfo The user information for whom the TOTP setup is being initiated.
     * @return A map containing the TOTP secret and the QR code URI.
     */
    @Override
    public Map<String, Object> initiateSetup(UserInfo userInfo) {
        if (userInfo.getPreferedMfaMethod() == MfaMethod.TOTP) {
            throw new IllegalStateException("TOTP is already set up for this user.");
        }
        String secret = totpAuthService.generateNewSecret();

        return Map.of("secret", secret,"qrCodeUri", totpAuthService.getQrCodeUri(secret, userInfo.getEmail()));

    }
    @Override
    public void sendVerificationCode(UserInfo userInfo) {
    }

    /**
     * Verifies the provided TOTP code against the user's secret.
     *
     * @param userInfo The user information containing the TOTP secret.
     * @param code The TOTP code to verify.
     * @return true if the code is valid, false otherwise.
     */
    @Override
    public boolean verifyCode(UserInfo userInfo, String code) {
        return totpAuthService.isValidCode(userInfo.getTotpSecret(), code);
    }

    /**
     * Gets the MFA method for this provider.
     *
     * @return MfaMethod.TOTP
     */
    @Override
    public MfaMethod getMethod() {
        return MfaMethod.TOTP;
    }
}
