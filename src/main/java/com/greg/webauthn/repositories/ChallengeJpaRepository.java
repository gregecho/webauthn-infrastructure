package com.greg.webauthn.repositories;

import com.greg.webauthn.model.entity.WebAuthnChallengeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface ChallengeJpaRepository extends JpaRepository<WebAuthnChallengeEntity, String> {

    Optional<WebAuthnChallengeEntity> findByRequestId(String requestId);

    @Modifying
    @Transactional
    @Query("DELETE FROM WebAuthnChallengeEntity c WHERE c.expiresAt < :now")
    int deleteExpired(Instant now);
}
