package com.skillhub.service;

import com.skillhub.entity.BackupCode;
import com.skillhub.repository.UserInfoRepository;
import com.skillhub.entity.UserInfo;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserInfoService {

    private static final Logger LOG = LoggerFactory.getLogger(UserInfoService.class);

    private final UserInfoRepository userInfoRepository;
    private final PasswordEncoder passwordEncoder;
    private final TwoFactorAuthService twoFactorAuthService;

    @Autowired
    public UserInfoService(UserInfoRepository userInfoRepository,
                           PasswordEncoder passwordEncoder,
                           TwoFactorAuthService twoFactorAuthService) {
        this.userInfoRepository = userInfoRepository;
        this.passwordEncoder = passwordEncoder;
        this.twoFactorAuthService = twoFactorAuthService;
    }

    /**
     * registerNewUser - register a new user with the provided details.
     */
    @Transactional
    public UserInfo registerNewUser(String firstName, String lastName, String email, String password, String phoneNumber) throws IllegalStateException {
        if (userInfoRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("Error: Email already in use.");
        }

        String token = UUID.randomUUID().toString();
        UserInfo newUser = UserInfo.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .phoneNumber(phoneNumber)
                .verificationToken(token)
                .build();
        // Save the new user to the database
        UserInfo saveUser = userInfoRepository.save(newUser);
        userInfoRepository.flush();

        LOG.info("New user registered: {}", saveUser.getEmail());
        return saveUser;
    }


    @Transactional
    public void update2FAuthentication(String email, String totpSecret) {
        UserInfo user = getUserByEmail(email);
        user.setTotpSecret(totpSecret);
        UserInfo updatedUser = userInfoRepository.save(user);
        userInfoRepository.flush();

        LOG.info("TOTP secret updated for user: {}", updatedUser.getEmail());
    }

    @Transactional
    public void updateEnable2FAuthentication(String email)throws IllegalStateException {
        UserInfo user = getUserByEmail(email);
        if (user.getTotpSecret() == null || user.getTotpSecret().isEmpty()) {
            throw new IllegalStateException("2FA is not set up for this user.");
        }
        user.set2faEnabled(true);
        UserInfo updatedUser = userInfoRepository.save(user);
        userInfoRepository.flush();

        LOG.info("2fa Enabled for user: {}", updatedUser.getEmail());
    }

    @Transactional
    public List<String> generateAndSetBackupCodes(String email){
        UserInfo user = getUserByEmail(email);
        user.getBackupCodes().clear();
        List<String> codes = new ArrayList<>();
        SecureRandom random = new SecureRandom();
        int numberOfCodes = 10;
        int codeLength = 6;
        // Generate 10 backup codes, each 6 digits long
        for (int i = 0; i < numberOfCodes; i++) {
            StringBuilder code = new StringBuilder();
            for (int j = 0; j < codeLength; j++) {
                int digit = random.nextInt(10); // Generate a digit from 0 to 9
                code.append(digit);
            }
            BackupCode backupCode = BackupCode.builder()
                    .code(passwordEncoder.encode(code.toString()))
                    .user(user)
                    .build();
            codes.add(code.toString());
            user.getBackupCodes().add(backupCode);
        }
        UserInfo updatedUser = userInfoRepository.save(user);
        userInfoRepository.flush();

        LOG.info("Backup codes generated and set for user: {}", updatedUser.getEmail());
        return codes;
    }

    @Transactional
    public void verifyUser(String token) throws IllegalStateException {
        UserInfo user = userInfoRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalStateException("Token is invalid or expired."));

        user.setVerified(true);
        user.setVerificationToken(null);
        userInfoRepository.save(user);
        userInfoRepository.flush();
        LOG.info("User verified: {}", user.getEmail());
    }

    public UserInfo getUserByEmail(String email) {
        return userInfoRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found with email: " + email));
    }

    @Transactional
    public void verifyBackupCode(String email, String code) throws IllegalStateException {
        UserInfo user = getUserByEmail(email);

        BackupCode backupCode = user.getBackupCodes().stream()
                .filter(bc -> !bc.isUsed() && passwordEncoder.matches(code, bc.getCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Invalid backup code or already used."));

        backupCode.setUsed(true);

        userInfoRepository.save(user);
        userInfoRepository.flush();
        LOG.info("Backup code verified for user: {}", user.getEmail());
    }

    @Transactional
    public boolean verifySecondFactor(String email, String code) {
        UserInfo user = getUserByEmail(email);
        if(isTotpCodeValid(user, code)){
            LOG.info("2FA code verified for user: {}", user.getEmail());
            return true;
        }
        try {
            verifyBackupCode(email, code);
            return true;
        } catch (IllegalStateException e) {
            LOG.error("2fa Code invalid !: {}", user.getEmail());
            return false;
        }
    }

    private boolean isTotpCodeValid(UserInfo user, String code) {
        TimeProvider timeProvider = new SystemTimeProvider();
        long currentBucket = timeProvider.getTime() / 30; // 30 seconds bucket
        Long lastTimestamp = user.getLast2faTimestamp();
        if (lastTimestamp != null && currentBucket <= lastTimestamp) {
            LOG.warn("re use 2fa code {}", user.getEmail());
            return false;
        }
        if (!twoFactorAuthService.isValidCode(user.getTotpSecret(), code)) {
            LOG.warn("Totp code is invalid ! {}", user.getEmail());
            return false;
        }

        user.setLast2faTimestamp(currentBucket);
        userInfoRepository.save(user);
        userInfoRepository.flush();
        LOG.info("Totp code is valid for user: {}", user.getEmail());
        return true;

    }

    public List<UserInfo> getAllUser() {
        return  userInfoRepository.findAll();
    }
}