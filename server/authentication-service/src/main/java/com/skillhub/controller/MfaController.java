package com.skillhub.controller;

import com.skillhub.dto.EnabledMfaRequest;
import com.skillhub.dto.SetupMfaRequest;
import com.skillhub.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/mfa")
public class MfaController {
    private final UserInfoService userInfoService;

    /**
     * Constructor for MfaController.
     *
     * @param userInfoService the service to handle user information and MFA operations
     */
    @Autowired
    public MfaController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    /**
     * Endpoint to initiate the setup of Multi-Factor Authentication (MFA).
     *
     * @param setupMfaRequest the request containing MFA setup details
     * @param authentication  the current user's authentication details
     * @return a response entity containing the setup data
     */
    @PostMapping("/setup")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> setupMfa(@Validated @RequestBody SetupMfaRequest setupMfaRequest, Authentication authentication) {
        Map<String, Object> setupData = userInfoService.initiateMfaSetup(authentication.getName(), setupMfaRequest.getMfaMethod());
        return ResponseEntity.ok(setupData);
    }

    /**
     * Endpoint to enable Multi-Factor Authentication (MFA) for the user.
     *
     * @param enabledMfaRequest the request containing MFA enabling details
     * @param authentication    the current user's authentication details
     * @return a response entity containing backup codes
     */
    @PostMapping("/enable")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, List<String>>> enableMfa(@Validated @RequestBody EnabledMfaRequest enabledMfaRequest, Authentication authentication) {
        List<String> backupCodes = userInfoService.enableMfa(authentication.getName(),enabledMfaRequest.getMfaMethod(), enabledMfaRequest.getMfaCode());
        return ResponseEntity.ok(Map.of("backupCodes", backupCodes));
    }

    /**
     * Endpoint to disable Multi-Factor Authentication (MFA) for the user.
     *
     * @param authentication the current user's authentication details
     * @return a response entity indicating the result of the operation
     */
    @PostMapping("/resend-mfa-code")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> sendMfaCode(Authentication authentication) {
        userInfoService.reSendMfaCode(authentication.getName());
        return ResponseEntity.ok().build();
    }
}
