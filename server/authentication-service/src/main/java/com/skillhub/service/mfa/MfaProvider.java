package com.skillhub.service.mfa;

import com.skillhub.entity.MfaMethod;
import com.skillhub.entity.UserInfo;

import java.util.Map;

public interface MfaProvider {
    Map<String, Object> initiateSetup(UserInfo userInfo);

    void sendVerificationCode(UserInfo userInfo);

    boolean verifyCode(UserInfo userInfo, String code);

    MfaMethod getMethod();
}
