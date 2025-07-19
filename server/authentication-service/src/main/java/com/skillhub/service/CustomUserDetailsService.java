package com.skillhub.service;

import com.skillhub.repository.UserInfoRepository;
import com.skillhub.entity.UserInfo;
import com.skillhub.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserInfoRepository userInfoRepository;


    /**
     * Constructor for CustomUserDetailsService.
     *
     * @param userInfoRepository the repository to access user information
     */
    @Autowired
    public CustomUserDetailsService(UserInfoRepository userInfoRepository) {
        this.userInfoRepository = userInfoRepository;
    }

    /**
     * Loads user details by email.
     *
     * @param email the email of the user
     * @return UserDetails object containing user information
     * @throws UsernameNotFoundException if the user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserInfo userInfo = userInfoRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found" + email));

        return new CustomUserDetails(
                userInfo.getUserId(),
                userInfo.getEmail(),
                userInfo.getPassword(),
                userInfo.getRole(),
                userInfo.getFirstName(),
                userInfo.getLastName()
        );

    }
}