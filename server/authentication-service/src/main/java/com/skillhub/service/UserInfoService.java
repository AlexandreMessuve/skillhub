package com.skillhub.service;

import com.skillhub.repository.UserInfoRepository;
import com.skillhub.entity.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserInfoService {

    private static final Logger LOG = LoggerFactory.getLogger(UserInfoService.class);

    private final UserInfoRepository userInfoRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserInfoService(UserInfoRepository userInfoRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userInfoRepository = userInfoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * registerNewUser - register a new user with the provided details.
     */
    @Transactional
    public UserInfo registerNewUser(String firstName, String lastName, String email, String password, String phoneNumber) {
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
                .doubleAuthEnabled(false)
                .verificationToken(token)
                .verified(false)
                .build();
        // Save the new user to the database
        UserInfo saveUser = userInfoRepository.save(newUser);
        userInfoRepository.flush();

        LOG.info("New user registered: {}", saveUser.getEmail());
        return saveUser;
    }

    @Transactional
    public void verifyUser(String token) {
        UserInfo user = userInfoRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalStateException("Error: Invalid verification token."));

        user.setVerified(true);
        user.setVerificationToken(null);
        userInfoRepository.save(user);
        userInfoRepository.flush();
        LOG.info("User verified: {}", user.getEmail());
    }

    public UserInfo getUserByEmail(String email) {
        return userInfoRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Error: User not found with email: " + email));
    }



    public List<UserInfo> getAllUser() {
        return  userInfoRepository.findAll();
    }

}