package com.skillhub.service;

import com.skillhub.entity.BackupCode;
import com.skillhub.entity.MfaMethod;
import com.skillhub.repository.UserInfoRepository;
import com.skillhub.entity.UserInfo;
import com.skillhub.service.mfa.MfaManagerService;
import com.skillhub.service.mfa.MfaProvider;
import com.skillhub.util.MyUtil;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class UserInfoService {

    private static final Logger LOG = LoggerFactory.getLogger(UserInfoService.class);

    private final UserInfoRepository userInfoRepository;
    private final PasswordEncoder passwordEncoder;
    private final MyUtil myUtil;
    private final PreAuthTokenService preAuthTokenService;
    private final MfaManagerService mfaManagerService;

    @Autowired
    public UserInfoService(UserInfoRepository userInfoRepository,
                           PasswordEncoder passwordEncoder,
                           MyUtil myUtil,
                           PreAuthTokenService preAuthTokenService,
                           MfaManagerService mfaManagerService) {
        this.userInfoRepository = userInfoRepository;
        this.passwordEncoder = passwordEncoder;
        this.myUtil = myUtil;
        this.preAuthTokenService = preAuthTokenService;
        this.mfaManagerService = mfaManagerService;
    }

    /**
     * registerNewUser - registers a new user with the provided details.
     * @param firstName the first name of the user
     * @param lastName the last name of the user
     * @param email the email of the user
     * @param password the password of the user
     * @param phoneNumber the phone number of the user
     * @return the newly registered UserInfo object
     * @throws IllegalStateException if the email is already in use
     */
    @Transactional
    public UserInfo registerNewUser(String firstName, String lastName, String email, String password, String phoneNumber) throws IllegalStateException {
        if (userInfoRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("Error: Email already in use.");
        }
        UserInfo newUser = UserInfo.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .phoneNumber(phoneNumber)
                .build();
        // Save the new user to the database
        UserInfo saveUser = saveUser(newUser);

        LOG.info("New user registered: {}", saveUser.getEmail());
        return saveUser;
    }

    /**
     * getUserByEmail - retrieves a user by their email address.
     * @param email the email of the user
     * @return the UserInfo object associated with the provided email
     * @throws IllegalStateException if no user is found with the provided email
     */
    public UserInfo getUserByEmail(String email) {
        return userInfoRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found with email: " + email));
    }

    /**
     * saveUser - saves the provided UserInfo object to the database.
     * @param userInfo the UserInfo object to save
     * @return the saved UserInfo object
     */
    @Transactional
    public UserInfo saveUser(UserInfo userInfo) {
        UserInfo savedUser = userInfoRepository.save(userInfo);
        userInfoRepository.flush();
        LOG.info("User saved: {}", savedUser.getEmail());
        return savedUser;
    }

    /**
     * updateUserInfo - updates the user information for the given UserInfo object.
     * @param userInfo the UserInfo object containing updated user information
     * @throws IllegalStateException if no user is found with the provided email
     */
    @Transactional
    public void updateUserInfo(UserInfo userInfo) {
        UserInfo existingUser = getUserByEmail(userInfo.getEmail());
        existingUser.setFirstName(userInfo.getFirstName());
        existingUser.setLastName(userInfo.getLastName());
        existingUser.setPhoneNumber(userInfo.getPhoneNumber());
        existingUser.setTotpSecret(userInfo.getTotpSecret());
        existingUser.setPreferedMfaMethod(userInfo.getPreferedMfaMethod());
        existingUser.setVerified(userInfo.isVerified());
        existingUser.setLast2faTimestamp(userInfo.getLast2faTimestamp());
        UserInfo updatedUser = saveUser(existingUser);
        LOG.info("User info updated for: {}", updatedUser.getEmail());
    }


    /**
     * verifyUser - verifies a user by checking the provided verification token.
     * @param token the verification token
     * @throws IllegalStateException if no user is found with the provided ID
     */
    @Transactional
    public void verifyUser(String token) throws IllegalStateException {
        UserInfo user = userInfoRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalStateException("No user found with the provided verification token."));

        if (user.isVerified()) {
            throw new IllegalStateException("User is already verified.");
        }
        user.setVerified(true);
        user.setVerificationToken(null); // Clear the verification token after successful verification
        updateUserInfo(user);
        LOG.info("User verified: {}", user.getEmail());
    }

    /**
     * initiateMfaSetup - initiates the setup process for Multi-Factor Authentication (MFA).
     * @param email the email of the user
     * @param method the MFA method to set up
     * @return a map containing the necessary information for MFA setup
     * @throws IllegalArgumentException if the MFA method is unsupported
     */
    @Transactional
    public Map<String, Object> initiateMfaSetup(String email, MfaMethod method) {
        UserInfo user = getUserByEmail(email);
        MfaProvider provider = mfaManagerService.getProvider(method);
        if (provider == null) {
            throw new IllegalArgumentException("Unsupported MFA method: " + method);
        }
        Map<String, Object> data = provider.initiateSetup(user);

        if (provider.getMethod() == MfaMethod.TOTP) {
            user.setTotpSecret(data.get("secret").toString());
            updateUserInfo(user);
        }

        return data;
    }

    /**
     * enableMfa - enables Multi-Factor Authentication (MFA) for the user.
     * @param email the email of the user
     * @param method the MFA method to enable
     * @param code the verification code for the MFA method
     * @return a list of backup codes generated for the user
     * @throws IllegalStateException if MFA is already enabled or the code is invalid
     */
    @Transactional
    public List<String> enableMfa(String email, MfaMethod method, String code) {
        UserInfo user = getUserByEmail(email);
        MfaProvider provider = mfaManagerService.getProvider(method);

        if (user.getPreferedMfaMethod() != MfaMethod.NONE) {
            throw new IllegalStateException("MFA is already enabled for this user.");
        }

        if (provider == null || !provider.verifyCode(user, code)) {
            throw new BadCredentialsException("Invalid MFA code provided.");
        }

        user.setPreferedMfaMethod(method);

        updateUserInfo(user);

        return generateAndSetBackupCodes(user);
    }

    public void sendEmailCode(String email) {
        UserInfo user = getUserByEmail(email);
        if (user.getPreferedMfaMethod() == MfaMethod.EMAIL) {
            throw new IllegalStateException("Email MFA is already enabled for this user.");
        }
        mfaManagerService.getProvider(MfaMethod.EMAIL).sendVerificationCode(user);
    }

    /**
     * verifySecondFactor - verifies the second factor for the user.
     * @param email the email of the user
     * @param code the code to verify
     * @return true if verification is successful, false otherwise
     */
    @Transactional
    public boolean verifySecondFactor(String email, String code) {
        UserInfo user = getUserByEmail(email);
        MfaMethod mfaMethod = user.getPreferedMfaMethod();
        MfaProvider provider = mfaManagerService.getProvider(user.getPreferedMfaMethod());
        TimeProvider timeProvider = new SystemTimeProvider();
        if (provider == null) {
            LOG.error("Unsupported MFA method: {}", mfaMethod);
            throw new IllegalArgumentException("Unsupported MFA method: " + mfaMethod);
        }
        if (mfaMethod == MfaMethod.NONE) {
            LOG.error("MFA is not enabled for user: {}", user.getEmail());
            throw new IllegalStateException("MFA is not enabled for this user.");
        }
        if(provider.getMethod() == MfaMethod.TOTP) {
            long currentBucket = timeProvider.getTime() / 30; // Assuming TOTP uses 30-second time steps
            Long last2faTimestamp = user.getLast2faTimestamp();
            if (last2faTimestamp != null && currentBucket <= last2faTimestamp) {
                LOG.error("2FA code already verified in the current time bucket for user: {}", user.getEmail());
                return false;
            }
        }

        if(provider.verifyCode(user, code)) {
            LOG.info("2FA code verified for user: {}", user.getEmail());
            if (provider.getMethod() == MfaMethod.TOTP) {
                // Update the last 2FA timestamp only for TOTP method
                user.setLast2faTimestamp(timeProvider.getTime() / 30); // Store as seconds since epoch
                updateUserInfo(user);
            }
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



    /**
     * verifyBackupCode - verifies a backup code for the user.
     * @param email the email of the user
     * @param code the backup code to verify
     * @throws IllegalStateException if the backup code is invalid or already used
     */
    @Transactional
    public void verifyBackupCode(String email, String code) throws IllegalStateException {
        UserInfo user = getUserByEmail(email);

        BackupCode backupCode = user.getBackupCodes().stream()
                .filter(bc -> !bc.isUsed() && passwordEncoder.matches(code, bc.getCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Invalid backup code or already used."));

        backupCode.setUsed(true);

        updateUserInfo(user);
        LOG.info("Backup code verified for user: {}", user.getEmail());
    }



    /**
     * generateAndSetBackupCodes - generates and sets backup codes for the user.
     * @param user the UserInfo object for which to generate backup codes
     * @return a list of generated backup codes
     */
    @Transactional
    public List<String> generateAndSetBackupCodes(UserInfo user) {
        user.getBackupCodes().clear();
        List<String> codes = new ArrayList<>();
        int numberOfCodes = 10;
        int codeLength = 6;
        // Generate 10 backup codes
        for (int i = 0; i < numberOfCodes; i++) {
            String code = myUtil.generateRandomCode(codeLength);
            BackupCode backupCode = BackupCode.builder()
                    .code(passwordEncoder.encode(code))
                    .user(user)
                    .build();
            codes.add(code);
            user.getBackupCodes().add(backupCode);
        }
        UserInfo updatedUser = saveUser(user);

        LOG.info("Backup codes generated and set for user: {}", updatedUser.getEmail());
        return codes;
    }

    public List<UserInfo> getAllUser() {
        return  userInfoRepository.findAll();
    }
}