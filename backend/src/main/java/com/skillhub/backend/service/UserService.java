package com.skillhub.backend.service;

import com.skillhub.backend.config.jwt.JwtTokenProvider;
import com.skillhub.backend.dto.user.UpdateUserPost;
import com.skillhub.backend.dto.user.UserGet;
import com.skillhub.backend.dto.user.RegisterUserPost;
import com.skillhub.backend.entity.Role;
import com.skillhub.backend.entity.User;
import com.skillhub.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Lazy
    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return  userRepository.findByEmail(email).orElseThrow( () -> new UsernameNotFoundException("User not found") );
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow( () -> new UsernameNotFoundException("User not found") );
    }

    public boolean updateUser(UpdateUserPost userPost) {
        try {
            User user = getUserByEmail(userPost.getEmail());
            user.setFirstName(userPost.getFirstName());
            user.setLastName(userPost.getLastName());
            user.setPhone(userPost.getPhoneNumber());
            userRepository.save(user);
            logger.info("User updated successfully: {}", userPost.getEmail());
            return true;
        } catch (Exception e) {
            logger.error("Error occurred while updating user: {}", e.getMessage());
            return false; // User update failed
        }
    }

    public boolean addUser(RegisterUserPost user) {
        try {
            if (checkUserNameExists(user.getEmail())){
                logger.info("User already exists");
                return false; // User with this email already exists
            }
            userRepository.save(registerUserDtoToUser(user));
            logger.info("User added successfully: {}", user.getEmail());
            return true;

        }catch (Exception e) {
            logger.error("Error occurred while adding user: {}",e.getMessage());
            return false; // User creation failed
        }
    }

    public boolean verifyUser(String email, String password) {
        return userRepository.findByEmail(email).map(user ->
                passwordEncoder.matches(password, user.getPassword()
                )).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public boolean checkUserNameExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public User registerUserDtoToUser(RegisterUserPost registerUserDto) {
        Role role = Role.COLLABORATOR;
        return User.builder()
                .email(registerUserDto.getEmail())
                .password(passwordEncoder.encode(registerUserDto.getPassword()))
                .role(role)
                .build();
    }
    public String generateToken(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtTokenProvider.generateToken(authentication);
    }

    public UserGet getUserDtoGetByEmail(String email){
        return userToUserDtoGet(getUserByEmail(email));
    }

    private UserGet userToUserDtoGet(User user) {
        return UserGet.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phoneNumber(user.getPhone())
                .lastName(user.getLastName())
                .firstName(user.getFirstName())
                .role(user.getRole())
                .build();
    }
}
