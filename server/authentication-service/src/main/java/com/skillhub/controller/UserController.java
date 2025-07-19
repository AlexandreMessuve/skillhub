package com.skillhub.controller;

import com.skillhub.dto.AccountVerifyPhoneRequest;
import com.skillhub.entity.UserInfo;
import com.skillhub.service.UserInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserInfoService userInfoService;

    /**
     * Constructor for UserController.
     *
     * @param userInfoService the service to handle user information operations
     */
    public UserController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    /**
     * Endpoint to send a verification code to the user's phone number.
     *
     * @param authentication the current user's authentication details
     * @return a response containing the status and message of the operation
     */
    @PostMapping("/send-verify-phone-number")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> sendVerifyPhoneNumber(Authentication authentication) {
        Map<String, Object> response = userInfoService.sendVerifyPhoneNumber(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-phone-number")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> verifyPhoneNumber(@Validated @RequestBody AccountVerifyPhoneRequest accountVerifyPhoneRequest, Authentication authentication) {
        UserInfo userInfo = userInfoService.getUserByEmail(authentication.getName());
        Map<String, String> response = userInfoService.verifyPhoneNumber(userInfo, accountVerifyPhoneRequest.getCode());
        return ResponseEntity.ok(response);
    }
}
