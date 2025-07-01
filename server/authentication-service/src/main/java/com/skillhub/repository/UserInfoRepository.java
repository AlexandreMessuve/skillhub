package com.skillhub.repository;

import com.skillhub.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, UUID> {
    /**
     * Finds a UserInfo entity by its email.
     *
     * @param EMAIL the email of the user
     * @return an Optional containing the UserInfo if found, or empty if not found
     */
    Optional<UserInfo> findByEmail(String EMAIL);

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
     * @param EMAIL the email of the user to delete
     */
    void deleteByEmail(@Param("email") String EMAIL);
}