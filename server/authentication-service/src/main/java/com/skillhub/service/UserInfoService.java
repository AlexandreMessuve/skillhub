package com.skillhub.service;

import com.skillhub.repository.UserInfoRepository;
import com.skillhub.entity.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserInfoService {

    private final UserInfoRepository userInfoRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Autowired
    public UserInfoService(UserInfoRepository userInfoRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userInfoRepository = userInfoRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * registerNewUser - register a new user with the provided details.
     */
    @Transactional
    public void registerNewUser(String firstName, String lastName, String email, String password, String phoneNumber) {
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

        // Send a welcome email to the new user
        emailService.sendVerifyEmailHtml(saveUser);

        // Optionally, you can log the registration or perform additional actions here
        System.out.println("New user registered: " + saveUser.getEmail());
    }

    public boolean verifyUser(String token) {
        UserInfo user = userInfoRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalStateException("Error: Invalid verification token."));

        if (user.isVerified()) {
            return true;
        }

        user.setVerified(true);
        user.setVerificationToken(null);
        userInfoRepository.save(user);
        return true;
    }

    public UserInfo getUserByEmail(String email) {
        return userInfoRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Error: User not found with email: " + email));
    }

    public List<UserInfo> getAllUser() {
        return  userInfoRepository.findAll();
    }

}