package com.skillhub.controller;

import com.skillhub.dto.TwoFactorRequest;
import com.skillhub.entity.UserInfo;
import com.skillhub.service.TwoFactorAuthService;
import com.skillhub.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final TwoFactorAuthService twoFactorAuthService;

    private final UserInfoService userInfoService;

    @Autowired
    public UserController(TwoFactorAuthService twoFactorAuthService, UserInfoService userInfoService) {
        this.twoFactorAuthService = twoFactorAuthService;
        this.userInfoService = userInfoService;
    }

    @PutMapping("/generate-2fa-qrcode")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> generate2faQrcode(Authentication authentication) {
        try {
            String secret = twoFactorAuthService.generateNewSecret();
            userInfoService.update2FAuthentication(authentication.getName(), secret);
            String qrCodeUri = twoFactorAuthService.getQrCodeUri(secret, authentication.getName());
            return ResponseEntity.ok(Map.of("qrCodeUri", qrCodeUri));
        }catch(RuntimeException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Qr Code generated failed"));
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/enable-2fa")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> enableTwoFactorAuthentication(@Validated @RequestBody TwoFactorRequest twoFactorRequest, Authentication authentication) {
        try {
            UserInfo userInfo = userInfoService.getUserByEmail(authentication.getName());
            if (userInfo.is2faEnabled()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message","2FA is already enabled"));
            }
            if (!userInfoService.verifySecondFactor(twoFactorRequest.getEmail(), twoFactorRequest.getCode())){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message","Invalid code"));
            }
            userInfoService.updateEnable2FAuthentication(authentication.getName());
            return ResponseEntity.ok(Map.of(
                    "message", "2FA enabled successfully",
                    "backupCodes", String.join(",", userInfoService.generateAndSetBackupCodes(authentication.getName()))
                    ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message","Failed to enable 2FA"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
