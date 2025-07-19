package com.skillhub.repository;

import com.skillhub.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, UUID> {
    /**
     * Finds a UserInfo entity by its email.
     *
     * @param email the email of the user
     * @return an Optional containing the UserInfo if found, or empty if not found
     */
    Optional<UserInfo> findByEmail(String email);

    /**
     * Finds a UserInfo entity by its username.
     *
     * @param token the validation token of the user
     * @return an Optional containing the UserInfo if found, or empty if not found
     */
    Optional<UserInfo> findByVerificationToken(String token);


    /**
     * Deletes a UserInfo entity by its email.
     *
     * @param email the email of the user to delete
     */
    @Transactional
    void deleteByEmail(String email);
}